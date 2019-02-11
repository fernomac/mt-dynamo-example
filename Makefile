# It's a shitty makefile!

.PHONY: refresh
refresh:
	mvn clean package exec:java -Dexec.mainClass="com.salesforce.dynamodb.example.Main"

.PHONY: run
run: target/mt-dynamo-example-1.0-SNAPSHOT.jar
	mvn exec:java -Dexec.mainClass="com.salesforce.dynamodb.example.Main"

.PHONY: build
build: 
	mvn clean package

target/mt-dynamo-example-1.0-SNAPSHOT.jar:
	mvn clean package

