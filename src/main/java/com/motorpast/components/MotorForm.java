package com.motorpast.components;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.CSSClassConstants;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FormValidationControl;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.TrackableComponentEventCallback;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.ValidationTrackerImpl;
import org.apache.tapestry5.ValidationTrackerWrapper;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.corelib.ClientValidation;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Submit;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportImpl;
import org.apache.tapestry5.corelib.internal.InternalFormSupport;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.BeanValidationContext;
import org.apache.tapestry5.internal.BeanValidationContextImpl;
import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.HeartbeatImpl;
import org.apache.tapestry5.internal.util.AutofocusValidationDecorator;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.util.ExceptionUtils;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * we have to take the original form from tapestry {@link Form} and remove some javascript stuff
 * now no javascript is used or initialized
 * @author dgerhardt
 *
 */
@Events(
{ EventConstants.PREPARE_FOR_RENDER, EventConstants.PREPARE, EventConstants.PREPARE_FOR_SUBMIT,
        EventConstants.VALIDATE, EventConstants.VALIDATE_FORM, EventConstants.SUBMIT, EventConstants.FAILURE,
        EventConstants.SUCCESS })
public class MotorForm implements FormValidationControl
{
    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String PREPARE_FOR_RENDER = EventConstants.PREPARE_FOR_RENDER;

    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String PREPARE_FOR_SUBMIT = EventConstants.PREPARE_FOR_SUBMIT;

    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String PREPARE = EventConstants.PREPARE;

    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String SUBMIT = EventConstants.SUBMIT;

    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String VALIDATE_FORM = EventConstants.VALIDATE_FORM;

    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String SUCCESS = EventConstants.SUCCESS;

    /**
     * @deprecated Use constant from {@link EventConstants} instead.
     */
    public static final String FAILURE = EventConstants.FAILURE;

    /**
     * Query parameter name storing form data (the serialized commands needed to
     * process a form submission).
     */
    public static final String FORM_DATA = "t:formdata";

    /**
     * Used by {@link Submit}, etc., to identify which particular client-side element (by element id)
     * was responsible for the submission. An empty hidden field is created, as needed, to store this value.
     * 
     * @since 5.2.0
     */
    public static final String SUBMITTING_ELEMENT_ID = "t:submit";

    /**
     * The context for the link (optional parameter). This list of values will
     * be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are
     * and made available to event handler
     * methods.
     */
    @Parameter
    private Object[] context;

    /**
     * The object which will record user input and validation errors. The object
     * must be persistent between requests
     * (since the form submission and validation occurs in a component event
     * request and the subsequent render occurs
     * in a render request). The default is a persistent property of the Form
     * component and this is sufficient for
     * nearly all purposes (except when a Form is rendered inside a loop).
     */
    @Parameter("defaultTracker")
    private ValidationTracker tracker;

    @Inject
    @Symbol(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED)
    private boolean clientLogicDefaultEnabled;

    /**
     * Controls when client validation occurs on the client, if at all. Defaults to {@link ClientValidation#BLUR}.
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private ClientValidation clientValidation = clientLogicDefaultEnabled ? ClientValidation.BLUR
            : ClientValidation.NONE;

    /**
     * If true (the default), then the JavaScript will be added to position the
     * cursor into the form. The field to
     * receive focus is the first rendered field that is in error, or required,
     * or present (in that order of priority).
     * 
     * @see SymbolConstants#FORM_CLIENT_LOGIC_ENABLED
     */
    @Parameter
    private boolean autofocus = clientLogicDefaultEnabled;

    /**
     * Binding the zone parameter will cause the form submission to be handled
     * as an Ajax request that updates the
     * indicated zone. Often a Form will update the same zone that contains it.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    /**
     * If true, then the Form's action will be secure (using an absolute URL with the HTTPs scheme) regardless
     * of whether the containing page itself is secure or not. This parameter does nothing
     * when {@linkplain SymbolConstants#SECURE_ENABLED security is disabled} (which is often
     * the case in development mode). This only affects how the Form's action attribute is rendered, there is
     * not (currently) a check that the form is actually submitted securely.
     */
    @Parameter
    private boolean secure;

    /**
     * Prefix value used when searching for validation messages and constraints.
     * The default is the Form component's
     * id. This is overridden by {@link org.apache.tapestry5.corelib.components.BeanEditForm}.
     * 
     * @see org.apache.tapestry5.services.FormSupport#getFormValidationId()
     */
    @Parameter
    private String validationId;

    /**
     * Object to validate during the form submission process. The default is the Form component's container.
     * This parameter should only be used in combination with the Bean Validation Library.
     */
    @Parameter
    private Object validate;

    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @Environmental
    private JavaScriptSupport jsSupport;

    @Inject
    private Request request;

    @Inject
    private ComponentSource source;

    @Inject
    @Symbol(InternalSymbols.PRE_SELECTED_FORM_NAMES)
    private String preselectedFormNames;

    @Persist(PersistenceConstants.FLASH)
    private ValidationTracker defaultTracker;

    @Inject
    @Symbol(SymbolConstants.SECURE_ENABLED)
    private boolean secureEnabled;

    private InternalFormSupport formSupport;

    private Element form;

    private Element div;

    // Collects a stream of component actions. Each action goes in as a UTF
    // string (the component
    // component id), followed by a ComponentAction

    private ComponentActionSink actionSink;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    @SuppressWarnings("unchecked")
    @Environmental
    private TrackableComponentEventCallback eventCallback;

    @Inject
    private ClientDataEncoder clientDataEncoder;

    private String clientId;

    // Set during rendering or submit processing to be the
    // same as the VT pushed into the Environment
    private ValidationTracker activeTracker;

    String defaultValidationId()
    {
        return resources.getId();
    }

    Object defaultValidate()
    {
        return resources.getContainer();
    }

    /**
     * Returns a wrapped version of the tracker parameter (which is usually bound to the
     * defaultTracker persistent field).
     * If tracker is currently null, a new instance of {@link ValidationTrackerImpl} is created.
     * The tracker is then wrapped, such that the tracker parameter
     * is only updated the first time an error is recorded into the tracker (this will typically
     * propagate to the defaultTracker
     * persistent field and be stored into the session). This means that if no errors are recorded,
     * the tracker parameter is not updated and (in the default case) no data is stored into the
     * session.
     * 
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-979">TAP5-979</a>
     * @return a tracker ready to receive data (possibly a previously stored tracker with field
     *         input and errors)
     */
    private ValidationTracker getWrappedTracker()
    {
        ValidationTracker innerTracker = tracker == null ? new ValidationTrackerImpl() : tracker;

        ValidationTracker wrapper = new ValidationTrackerWrapper(innerTracker)
        {
            private boolean saved = false;

            private void save()
            {
                if (!saved)
                {
                    tracker = getDelegate();

                    saved = true;
                }
            }

            @Override
            public void recordError(Field field, String errorMessage)
            {
                super.recordError(field, errorMessage);

                save();
            }

            @Override
            public void recordError(String errorMessage)
            {
                super.recordError(errorMessage);

                save();
            }
        };

        return wrapper;
    }

    public ValidationTracker getDefaultTracker()
    {
        return defaultTracker;
    }

    public void setDefaultTracker(ValidationTracker defaultTracker)
    {
        this.defaultTracker = defaultTracker;
    }

    void setupRender()
    {
        FormSupport existing = environment.peek(FormSupport.class);

        if (existing != null)
            throw new TapestryException(messages.get("nesting-not-allowed"), existing, null);
    }

    void beginRender(MarkupWriter writer)
    {
        Link link = resources.createFormEventLink(EventConstants.ACTION, context);

        String actionURL = secure && secureEnabled ? link.toAbsoluteURI(true) : link.toURI();

        actionSink = new ComponentActionSink(logger, clientDataEncoder);

        clientId = javascriptSupport.allocateClientId(resources);

        // Pre-register some names, to prevent client-side collisions with function names
        // attached to the JS Form object.

        IdAllocator allocator = new IdAllocator();

        preallocateNames(allocator);

        formSupport = createRenderTimeFormSupport(clientId, actionSink, allocator);

        if (zone != null)
            linkFormToZone(link);

        activeTracker = getWrappedTracker();

        environment.push(FormSupport.class, formSupport);
        environment.push(ValidationTracker.class, activeTracker);
        environment.push(BeanValidationContext.class, new BeanValidationContextImpl(validate));

        if (autofocus)
        {
            ValidationDecorator autofocusDecorator = new AutofocusValidationDecorator(
                    environment.peek(ValidationDecorator.class), activeTracker, jsSupport);
            environment.push(ValidationDecorator.class, autofocusDecorator);
        }

        // Now that the environment is setup, inform the component or other
        // listeners that the form
        // is about to render.

        resources.triggerEvent(EventConstants.PREPARE_FOR_RENDER, context, null);

        resources.triggerEvent(EventConstants.PREPARE, context, null);

        // Save the form element for later, in case we want to write an encoding
        // type attribute.

        form = writer.element("form", "id", clientId, "method", "post", "action", actionURL);

        if ((zone != null || clientValidation != ClientValidation.NONE) && !request.isXHR())
            writer.attributes("onsubmit", MarkupConstants.WAIT_FOR_PAGE);

        resources.renderInformalParameters(writer);

        div = writer.element("div", "class", CSSClassConstants.INVISIBLE);

        for (String parameterName : link.getParameterNames())
        {
            String value = link.getParameterValue(parameterName);

            writer.element("input", "type", "hidden", "name", parameterName, "value", value);
            writer.end();
        }

        writer.end(); // div

        environment.peek(Heartbeat.class).begin();
    }

    @HeartbeatDeferred
    private void linkFormToZone(Link link)
    {
        clientBehaviorSupport.linkZone(clientId, zone, link);
    }

    /**
     * Creates an {@link org.apache.tapestry5.corelib.internal.InternalFormSupport} for
     * this Form. This method is used
     * by {@link org.apache.tapestry5.corelib.components.FormInjector}.
     * <p>
     * This method may also be invoked as the handler for the "internalCreateRenderTimeFormSupport" event.
     * 
     * @param clientId
     *            the client-side id for the rendered form
     *            element
     * @param actionSink
     *            used to collect component actions that will, ultimately, be
     *            written as the t:formdata hidden
     *            field
     * @param allocator
     *            used to allocate unique ids
     * @return form support object
     */
    @OnEvent("internalCreateRenderTimeFormSupport")
    InternalFormSupport createRenderTimeFormSupport(String clientId, ComponentActionSink actionSink,
            IdAllocator allocator)
    {
        return new FormSupportImpl(resources, clientId, actionSink, clientBehaviorSupport,
                clientValidation != ClientValidation.NONE, allocator, validationId);
    }

    void afterRender(MarkupWriter writer)
    {
        environment.peek(Heartbeat.class).end();

        formSupport.executeDeferred();

        String encodingType = formSupport.getEncodingType();

        if (encodingType != null)
            form.forceAttributes("enctype", encodingType);

        writer.end(); // form

        div.element("input", "type", "hidden", "name", FORM_DATA, "value", actionSink.getClientData());

        if (autofocus)
            environment.pop(ValidationDecorator.class);
    }

    void cleanupRender()
    {
        environment.pop(FormSupport.class);

        formSupport = null;

        environment.pop(ValidationTracker.class);

        activeTracker = null;

        environment.pop(BeanValidationContext.class);
    }

    @SuppressWarnings(
    { "unchecked", "InfiniteLoopStatement" })
    @Log
    Object onAction(EventContext context) throws IOException
    {
        activeTracker = getWrappedTracker();

        activeTracker.clear();

        formSupport = new FormSupportImpl(resources, validationId);

        environment.push(ValidationTracker.class, activeTracker);
        environment.push(FormSupport.class, formSupport);
        environment.push(BeanValidationContext.class, new BeanValidationContextImpl(validate));

        Heartbeat heartbeat = new HeartbeatImpl();

        environment.push(Heartbeat.class, heartbeat);

        heartbeat.begin();

        try
        {
            resources.triggerContextEvent(EventConstants.PREPARE_FOR_SUBMIT, context, eventCallback);

            if (eventCallback.isAborted())
                return true;

            resources.triggerContextEvent(EventConstants.PREPARE, context, eventCallback);

            if (eventCallback.isAborted())
                return true;

            executeStoredActions();

            heartbeat.end();

            formSupport.executeDeferred();

            fireValidateFormEvent(context, eventCallback);

            if (eventCallback.isAborted())
                return true;

            // Let the listeners know about overall success or failure. Most
            // listeners fall into
            // one of those two camps.

            // If the tracker has no errors, then clear it of any input values
            // as well, so that the next page render will be "clean" and show
            // true persistent data, not value from the previous form
            // submission.

            if (!activeTracker.getHasErrors())
                activeTracker.clear();

            resources.triggerContextEvent(activeTracker.getHasErrors() ? EventConstants.FAILURE
                    : EventConstants.SUCCESS, context, eventCallback);

            // Lastly, tell anyone whose interested that the form is completely
            // submitted.

            if (eventCallback.isAborted())
                return true;

            resources.triggerContextEvent(EventConstants.SUBMIT, context, eventCallback);

            return eventCallback.isAborted();
        }
        finally
        {
            environment.pop(Heartbeat.class);
            environment.pop(FormSupport.class);

            environment.pop(ValidationTracker.class);

            environment.pop(BeanValidationContext.class);

            activeTracker = null;
        }
    }

    private void fireValidateFormEvent(EventContext context, TrackableComponentEventCallback callback)
            throws IOException
    {
        fireValidateEvent(EventConstants.VALIDATE, context, callback);

        if (callback.isAborted())
            return;

        fireValidateEvent(EventConstants.VALIDATE_FORM, context, callback);
    }

    private void fireValidateEvent(String eventName, EventContext context, TrackableComponentEventCallback callback)
    {
        try
        {
            resources.triggerContextEvent(eventName, context, callback);
        }
        catch (RuntimeException ex)
        {
            ValidationException ve = ExceptionUtils.findCause(ex, ValidationException.class);

            if (ve != null)
            {
                ValidationTracker tracker = environment.peek(ValidationTracker.class);

                tracker.recordError(ve.getMessage());

                return;
            }

            throw ex;
        }
    }

    /**
     * Pulls the stored actions out of the request, converts them from MIME
     * stream back to object stream and then
     * objects, and executes them.
     */
    private void executeStoredActions()
    {
        String[] values = request.getParameters(FORM_DATA);

        if (!request.getMethod().equals("POST") || values == null)
            throw new RuntimeException(messages.format("invalid-request", FORM_DATA));

        // Due to Ajax (FormInjector) there may be multiple values here, so
        // handle each one individually.

        for (String clientEncodedActions : values)
        {
            if (InternalUtils.isBlank(clientEncodedActions))
                continue;

            logger.debug("Processing actions: {}", clientEncodedActions);

            ObjectInputStream ois = null;

            Component component = null;

            try
            {
                ois = clientDataEncoder.decodeClientData(clientEncodedActions);

                while (!eventCallback.isAborted())
                {
                    String componentId = ois.readUTF();
                    ComponentAction action = (ComponentAction) ois.readObject();

                    component = source.getComponent(componentId);

                    logger.debug("Processing: {} {}", componentId, action);

                    action.execute(component);

                    component = null;
                }
            }
            catch (EOFException ex)
            {
                // Expected
            }
            catch (Exception ex)
            {
                Location location = component == null ? null : component.getComponentResources().getLocation();

                throw new TapestryException(ex.getMessage(), location, ex);
            }
            finally
            {
                InternalUtils.close(ois);
            }
        }
    }

    public void recordError(String errorMessage)
    {
        getActiveTracker().recordError(errorMessage);
    }

    public void recordError(Field field, String errorMessage)
    {
        getActiveTracker().recordError(field, errorMessage);
    }

    public boolean getHasErrors()
    {
        return getActiveTracker().getHasErrors();
    }

    public boolean isValid()
    {
        return !getActiveTracker().getHasErrors();
    }

    private ValidationTracker getActiveTracker()
    {
        return activeTracker != null ? activeTracker : getWrappedTracker();
    }

    public void clearErrors()
    {
        getActiveTracker().clear();
    }

    // For testing:

    void setTracker(ValidationTracker tracker)
    {
        this.tracker = tracker;
    }

    /**
     * Forms use the same value for their name and their id attribute.
     */
    public String getClientId()
    {
        return clientId;
    }

    @Inject
    private ComponentSource componentSource;

    private void preallocateNames(IdAllocator idAllocator)
    {
        for (String name : TapestryInternalUtils.splitAtCommas(preselectedFormNames))
        {
            idAllocator.allocateId(name);
        }

        Component activePage = componentSource.getActivePage();

        // This is unlikely but may be possible if people override some of the standard
        // exception reporting logic.

        if (activePage == null)
            return;

        ComponentResources activePageResources = activePage.getComponentResources();

        try
        {

            activePageResources.triggerEvent(EventConstants.PREALLOCATE_FORM_CONTROL_NAMES, new Object[]
            { idAllocator }, null);
        }
        catch (RuntimeException ex)
        {
            logger.error(
                    String.format("Unable to obtrain form control names to preallocate: %s",
                            InternalUtils.toMessage(ex)), ex);
        }
    }
}
