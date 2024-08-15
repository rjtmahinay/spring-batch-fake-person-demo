# Tech Fusion: Spring Batch Demonstration

This is a code demonstration of Spring Batch using a fake person data from [Java Faker](https://github.com/DiUS/java-faker). The demonstration was done during
the meetup event of [Java User Group Philippines](https://linktr.ee/jugph), [Data Engineering Pilipinas](https://www.meetup.com/data-engineering-pilipinas/), and [Manila Apache Kafka](https://www.meetup.com/Manila-Kafka/).

## Prerequisites

The application needs a database to store the data. You may opt to manually install a MySQL Database or use the container approach using Podman Desktop.

* [MySQL Database - Manual Install](https://dev.mysql.com/doc/mysql-installer/en/)
* [Podman Desktop](https://podman-desktop.io/docs/installation)

### MySQL via Podman Desktop

After installation of Podman Desktop, perform the following commands:

<i>Pull the latest MySQL Image</i>
```shell
podman pull mysql:latest
```

<i>Run a MySQL Database Container</i>
```shell
podman run -d -p 3308:3306 --name=<REPLACEME: container name> -e MYSQL_ROOT_PASSWORD=1234 mysql
```

<i>Make sure the MySQL Database Container is running. The grep command below will work in Linux or Git Bash</i>

```shell
podman podman ps | grep '<REPLACEME: container name>'
```

### Managing the Database

* Interact with your MySQL Database
```shell
podman exec -it <REPLACEME: container name> bash
```
* Login to the database and the sample root password

```shell
mysql -uroot -p
```

* After logging in you can now do SQL operations.

### Spring Batch Application

* Fork this project
* Go to the project directory
* Run the following commands below:

<i>Run the Spring Batch Application</i>
```shell
./gradlew clean build bootRun
```

<i>Verify the data in your MySQL Database</i>

```mysql

-- Execute this if you aren't in the DEMO schema
USE demo;

-- Retrieve all person data
SELECT * FROM PERSON;

-- Verify the count. The result should be 500000
SELECT COUNT(*) FROM PERSON;
```

