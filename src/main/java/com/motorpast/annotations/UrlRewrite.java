package com.motorpast.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UrlRewrite
{
    public String[] mappings();

    public boolean putInSitemap() default true;

    public String[] sitemap() default {};
}
