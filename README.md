# GPITF 111 Report Transformation Service
## Overview
This service provides an interface for transforming a valid 111 XML Report into a CDS API 2.0 Report and viewing the result.

## Source Code Location
The repository for this project is located in a public GitLab space here: https://gitlab.com/ems-test-harness/gpitf-reports

## Build Steps
This project is configured to run on port 8088. For local machines, this can be accessed at http://localhost:8088. To run the Reports Service, simply run the maven task:

```bash
mvn spring-boot:run
```

## Project Structure
### Implementation
The 111 Transformation Service is a Java Spring Application. It is split into three major layers:

- Controller - This contains the end-point from where the transformation/user interface is invoked
- Service Layer - This contains services for creating each part of the encounter report
- Transformation Layer - This contains transformations from the XML Model to the FHIR model. Java classes for the XML model are deployed as a bintray artifact.

There are also packages for:
- Utilities
- Configuration (For the spring, security and fhir server)
### UI
The UI is written as a set of thymeleaf templates found in `resources/templates`.

### Tests
A component level test is implemented with test XML files to ensure that the correct resources are created.

Unit tests are also provided for every transformer.

## Licence

Unless stated otherwise, the codebase is released under [the MIT License][mit].
This covers both the codebase and any sample code in the documentation.

The documentation is [© Crown copyright][copyright] and available under the terms
of the [Open Government 3.0][ogl] licence.

[rvm]: https://www.ruby-lang.org/en/documentation/installation/#managers
[bundler]: http://bundler.io/
[mit]: LICENCE
[copyright]: http://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/
[ogl]: http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
