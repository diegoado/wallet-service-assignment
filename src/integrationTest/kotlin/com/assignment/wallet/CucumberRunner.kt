package com.assignment.wallet

import io.cucumber.core.options.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/assignment/wallet/steps")
@ConfigurationParameter(key = Constants.CUCUMBER_PROPERTIES_FILE_NAME, value = "cucumber.yaml")
class CucumberRunner
