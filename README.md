# BusinessRulesValidationCore

Core functionality for validating digital green certificates with the New Business Rule Format described in https://github.com/Federal-Ministry-of-Health-AT/green-pass-overview

The response from the endpoints provided there can be parsed with a BusinessRulesContainer provided in https://github.com/ehn-dcc-development/hcert-kotlin which will contain only a single BusinessRule entry for the New Business Rule Format. This BusinessRule contains a String which can then be provided to the BusinessRuleValidator provided in this library.

A simple pseudocode implementation looks like this:

```
val responseFromEndpoint: BusinessRulesContainer = ...
val ruleString: String = responseFromEndpoint.rules.first().rule
val parsedRules: BusinessRuleContainer = BusinessRuleContainer.fromData(ruleString)!!
val validator = BusinessRulesValidator(businessRules: parsedRules, valueSets: ...)
val validationResult = validator.evaluateCertificate(...)
```

The evaluateCertificate method in BusinessRuleValidator can then be used to evaluate a certificate for one or more profiles in a given region (e.g. W for Wien/Vienna).

See the provided unit tests for detailed examples on the usage and format of this library. Alternatively you can also check the usage of this library in the official Austrian Green Pass app for Android on https://github.com/BRZ-GmbH/CovidCertificate-App-Android

# Integration

At the moment we recommend integrating this package through Git submodules as we do not yet provide a packaged version of this library.