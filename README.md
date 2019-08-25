# record-jar-converter
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.pro-crafting.tools/jsonpretty-web/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.pro-crafting.tools/jsonpretty-web)
[![](https://images.microbadger.com/badges/image/postremus/jsonpretty-web.svg)](https://microbadger.com/images/postremus/jsonpretty-web "Get your own image badge on microbadger.com")
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Quarkus based web service for converting uggly json into prettified json.

## Installation
Use the official docker image:
postremus/jsonpretty-web
Tags for this image correspond to the maven versions, e.g. the 1.0.0 labeled docker image contains the 1.0.0 maven artifact.

Run it via:
````
docker run -p 8080:8080 postremus/jsonpretty-web:1.0.0
````

And then visit https://localhost.8080

You can find a hosted version of this prettifier at [Jsonpretty](https://json.pro-crafting.com)

## Building:

The project is built using maven.

The following maven phases and goals will run quarkus in dev mode. The prettifier is reachable at https://localhost:8080
````
mvn clean compile quarkus:dev
````

For building the docker image, use profile docker.
````
mvn clean install -Pdocker
````


## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/Postremus/record-jar-converter/tags). 

## Authors

* **Martin Panzer** - *Initial work* - [Postremus](https://github.com/Postremus)

See also the list of [contributors](https://github.com/Postremus/record-jar-converter/contributors) who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* [Docker](https://docker.io)
* Quarkus - the application server behind this project [Quarkus](https://quarkus.io)
* All the people behind [Maven](https://maven.apache.org/team-list.html) and [Java](https://java.net/people).