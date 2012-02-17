package com.motorpast.services.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.services.RequestGlobals;

import com.motorpast.additional.MotorUtils;

@EagerLoad
public class SecurityServiceImpl implements SecurityService
{
    private RequestGlobals requestGlobals;

    /**
     * defined css classes - have to match with css
     */
    private static enum CssClasses {
        SimpleHide("text1"),
        SimpleShow("text2"),
        HigherShow("text3"),
        HigherHide("text4");

        private final String cssClass;

        private CssClasses(final String cssClass) {
            this.cssClass = cssClass;
        }

        public final String css() {
            return cssClass;
        }
    };

    /**
     * all combinations for textfields on indexpage (first two fields of each combination are the non-fakes!!!)
     */
    private final int[][] combosForTextboxes;

    /**
     * holds the css-hide combos
     */
    private final String[] hide;

    /**
     * holds the css-show combos
     */
    private final String[] show;


    /**
     * an application constant which defines the time between a valid form-submit or a suspicious one
     */
    private final long antiSpambotTime;

 
    public SecurityServiceImpl(final RequestGlobals requestGlobals, final long antiSpambotTime) {
        this.requestGlobals = requestGlobals;
        this.antiSpambotTime = antiSpambotTime;

        combosForTextboxes = new int[][] {
            {0,1,2,3},
            {1,2,0,3},
            {2,3,0,1},
            {0,2,1,3},
            {1,3,0,2},
            {0,3,1,2}
        };

        hide = new String[] {
            CssClasses.SimpleHide.css(),
            CssClasses.SimpleShow.css() + " " + CssClasses.HigherHide.css(),
            CssClasses.HigherShow.css() + " " + CssClasses.HigherHide.css(),
            CssClasses.HigherHide.css() + " " + CssClasses.HigherShow.css(),
            CssClasses.HigherHide.css() + " " + CssClasses.SimpleShow.css()
        };

        show = new String[] {
            CssClasses.SimpleShow.css(),
            CssClasses.SimpleShow.css() + " " + CssClasses.SimpleHide.css(),
            CssClasses.SimpleHide.css() + " " + CssClasses.SimpleShow.css(),
            CssClasses.HigherShow.css() + " " + CssClasses.SimpleHide.css(),
            CssClasses.SimpleHide.css() + " " + CssClasses.HigherShow.css()
        };
    }


    @Override
    public int[] getCombos() {
        // get a random field in combosForTextboxes
        return combosForTextboxes[MotorUtils.getRandomInt(6)];
    }

    @Override
    public String getRandomCss(final boolean isHide) {
        final int index = MotorUtils.getRandomInt(5);

        if(isHide) {
            return hide[index];
        } else {
            return show[index];
        }
    }

    @Override
    public String generateToken(/*final HttpServletRequest httpServletRequest*/) {
        final HttpServletRequest httpServletRequest = requestGlobals.getHTTPServletRequest();
        final HttpSession httpSession = httpServletRequest.getSession();

        final String id = httpSession != null ? httpSession.getId() : String.valueOf(System.currentTimeMillis());

        try {
            long current = System.currentTimeMillis();

            byte[] now = new Long(current).toString().getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(id.getBytes());
            md.update(now);

            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Convert a byte array to a String of hexadecimal digits and return it.
     * @param buffer The byte array to be converted
     */
    private String toHex(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);

        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 0x0f, 16));
        }

        return sb.toString();
    }

    @Override
    public long generateTimestamp() {
        final Date now = new Date();

        return now.getTime();
    }

    @Override
    public long generateCheckTimestamp() {
        final Date now = new Date();
        final long checkTimestamp = now.getTime() - antiSpambotTime;

        return checkTimestamp;
    }
}
