Components:
* [Camunda form](https://docs.camunda.io/docs/components/modeler/forms/camunda-forms-reference/)
* HTML website with using [form-js](https://bpmn.io/toolkit/form-js/) to render the form and submit on request
* REST endpoint to take the data from the form and start a process instance

Requirements:
* Camunda Platform 8
* Java >= 17
* Maven

Run:
* Download/clone the code.
* Create a Camunda 8 SaaS cluster.
* Set API client connection details in the file `application.properties`.
* Run the application:

```
mvn package exec:java
```

* Open http://localhost:8080/