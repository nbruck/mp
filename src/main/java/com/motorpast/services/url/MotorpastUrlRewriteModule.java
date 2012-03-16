package com.motorpast.services.url;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.RewriteRuleApplicability;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriteContext;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;

import com.motorpast.pages.ConfirmationPage;
import com.motorpast.pages.ErrorPage;
import com.motorpast.pages.HelpPage;
import com.motorpast.pages.Index;
import com.motorpast.pages.ResultPage;
import com.motorpast.pages.SiteNoticePage;

/**
 * simple url rewriting using old api
 */
@SuppressWarnings("deprecation")
public class MotorpastUrlRewriteModule
{
    @EagerLoad
    public static AnnotationScannerService buildAnnotationScannerService() {
        return new AnnotationScannerServiceImpl(
            Index.class,
            HelpPage.class,
            SiteNoticePage.class,
            ErrorPage.class,
            ResultPage.class,
            ConfirmationPage.class
        );
    }

    public static void contributeURLRewriter(
        OrderedConfiguration<URLRewriterRule> configuration,
        final AnnotationScannerService annotationScannerService,
        final PageRenderLinkSource pageRenderLinkSource
    ) {
        URLRewriterRule rule1 = new URLRewriterRule()
        {
            public Request process(Request request, URLRewriteContext context)
            {
                final String path = request.getPath().toLowerCase();
                final String key = path.replace(request.getContextPath() ,"");
                final String rewritten = annotationScannerService.getOriginalToRewritten().get(key);

                if(rewritten != null) {
                    return new SimpleRequestWrapper(request, path.replace(path, rewritten));
                } else {
                    return request;
                }
            }

            public RewriteRuleApplicability applicability() {
                return RewriteRuleApplicability.OUTBOUND;
            }
        };

        URLRewriterRule rule2 = new URLRewriterRule()
        {
            public Request process(Request request, URLRewriteContext context)
            {
                final String path = request.getPath().toLowerCase();
                final String key = path.replace(request.getContextPath() ,"");
                final String rewritten = annotationScannerService.getRewrittenToOriginal().get(key);

                if(rewritten != null) {
                    return new SimpleRequestWrapper(request, path.replace(path, rewritten));
                } else {
                    return request;
                }
            }

            public RewriteRuleApplicability applicability() {
                return RewriteRuleApplicability.INBOUND;
            }

        };

        configuration.add("rule1", rule1);
        configuration.add("rule2", rule2);
    }
}
