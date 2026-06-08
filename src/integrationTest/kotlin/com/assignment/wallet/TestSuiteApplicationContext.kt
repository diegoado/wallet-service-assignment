package com.assignment.wallet

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@CucumberContextConfiguration
@ActiveProfiles("integration-test")
@SpringBootTest(
  classes = [
    WalletApplication::class
  ],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class TestSuiteApplicationContext
