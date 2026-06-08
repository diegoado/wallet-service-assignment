pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

plugins {
  id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.17"
}

gitHooks {
  preCommit {
    tasks("ktlintCheck")
  }
  hook("pre-push") {
    tasks("check")
  }
  commitMsg {
    conventionalCommits {
      defaultTypes()
      types("release")
    }
  }
  createHooks(true)
}

rootProject.name = "wallet-service-assignment"
