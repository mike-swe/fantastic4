package com.revature.fantastic4.e2e;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("com.revature.fantastic4.e2e")
@ConfigurationParameter(key = "cucumber.glue", value = "com.revature.fantastic4.e2e.steps,com.revature.fantastic4.e2e.config")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, html:target/cucumber-report.html")
@ConfigurationParameter(key = "cucumber.features", value = "classpath:features")
public class CucumberE2ERunner {
}
