# Experimenting with AWS Lambda (Java) and Serverless Framework

[Serverless](https://www.serverless.com/framework/docs/getting-started/) is a node.js based framework that facilitates creating, deploying, and managing serverless functions regardless the Cloud Provider. In this tutorial we will be using AWS.

# Pre-requisites

1. Install [node js and npm.](https://nodejs.org/en/download/)
2. Have a JDK installation on your system.
3. Set the JAVA_HOME environment variable pointing to your JDK installation.
4. Download [Apache Maven.](http://maven.apache.org/install.html)
5. Install AWS CLI and configure it using your Admin User. [Here is a short video](https://www.youtube.com/watch?v=fLlUfN61K6M) on how to do this.

## Confirm your installations

### Using Windows Powershell

- Node js Version
`node -v`

- npm Version
`npm -v`

- Maven and Java Version
`mvn -v`

- Check all environment variables
`dir env:`

- Check specific environment variable, for example PATH
`$env:PATH`

- AWS CLI Version
`aws --version`


# Install and Setup Serverless Framework

You can install using npm
`npm install -g serverless`

And confirm your framework version, after installation is complete, with the command
`serverless -v`

## Create the AWS User for Serverless Framework

As we are going to work with AWS, we need to have an Account. Serverless needs to use AWS credentials for an IAM user, so we recommend creating a User with no access, and we can add permissions as we go. This can be done manually using the [AWS IAM Console](https://console.aws.amazon.com/iamv2/home#/home), or via CloudFormation Stack.

### Manually in AWS IAM Console
1. Open the [AWS IAM Console](https://console.aws.amazon.com/iamv2/home#/home)
2. Search for "Users" option under the "Access Management" menu, on the left panel.
3. Click the "Add users" button (top right).
4. Provide a User name, and Select AWS Access type as "Access key - Programmatic access".
5. Select "Create Group", provide a name, and select the following Policies: AmazonS3FullAccess, CloudWatchLogsFullAccess, IAMFullAccess, AmazonDynamoDBFullAccess. If you want to do Lambda Proxy Integration (API Gateway+Lambda) then you need to add AmazonAPIGatewayAdministrator policy too.
6. Keep clicking Next until step 4 where you see the User details, and push the "Create user" button.
7. Take note of the AccessKey and SecretAccessKey. Then click "Close".
8. Once you are back on the Users section, click on the recently created user, and add the following "Inline Policy" to allow control on CloudFormation from Serverless Framework:
`{ "Version": "2012-10-17", "Statement": [ { "Sid": "serverless01", "Effect": "Allow", "Action": "cloudformation:*", "Resource": "*" } ] }`
9. Create anothe inline Policy for Lambda:
`{ "Version": "2012-10-17", "Statement": [ { "Sid": "serverless02", "Effect": "Allow", "Action": "lambda:*", "Resource": "*" } ] `


### Via CloudFormation Stack

The CloudFormation stack can be created using a template and uploading it directly into the AWS Console or via AWS CLI (preferred). In this repository, we are providing a template that includes all the required policies.

#### CloudFormation in AWS Console
1. Open [AWS CloudFormation Console](https://us-east-2.console.aws.amazon.com/cloudformation/)
2. Click on "Create Stack with new Resources" button.
3. Select "Template is ready" option.
4. Select "Upload a template file"
5. Choose the file "iam-template.yaml" provided in this repository under CloudFormationTemplates.
6. Click Next until you can hit the "Create Stack" button
7. You will get the UserAccessKey and UserSecretKey in the "Outputs" section.


#### CloudFormation in AWS CLI (preferred)
If you already have your *AWS CLI* configured with: *a)* an AWS Root Account (not recommended) or *b)* an AWS User with Administrator Access, then you can create the stack from the CLI. Remember the stack will be created in the configured region.

1. Validate the Template
`aws cloudformation validate-template --template-body file://iam-template.yaml`

2. Create CloudFormation Stack
`aws cloudformation create-stack --stack-name serverless-fmwk-iam --template-body file://iam-template.yaml --capabilities CAPABILITY_NAMED_IAM`

3. Get UserAccessKey and UserSecretKey from the Stack Outputs
`aws cloudformation describe-stacks --stack-name serverless-fmwk-iam --query "Stacks[0].Outputs" --output text`

- If you want to check for only specific Output key, like UserSecretKey:
`aws cloudformation describe-stacks --stack-name serverless-fmwk-iam --query "Stacks[0].Outputs[?OutputKey=='UserSecretKey'].OutputValue" --output text`


## Setup Serverless Framework for AWS

Going back to the console (Powershell), we provide the AWS credentials to Serverless Framework and setup a new AWS User profile.

`serverless config credentials --provider aws --key <key> --secret <secret> --profile serverlessUser --overwrite`

NOTE: AWS User Profiles are stored in the credentials file under aws folder.

`ls ~/.aws/credentials`

# Create the Java Project

This example will generate scaffolding for a service (named java-lambda-service) with AWS as a provider and Java as runtime. The scaffolding will be generated in the specified directory (java-demo-project). This directory will be created if not present. Otherwise Serverless will use the already present directory. Your new service will have a default stage called dev and a default region inside that stage called us-east-1.

`serverless create --template aws-java-maven --name java-lambda-service --path java-demo-project`

- Move to the created directory (java-demo-project)
`cd java-demo-project`

- Get information about your deployed service
`serverless info --aws-profile serverlessUser`

# Deploy the Stack

Since deployment of the stack is essentially setting up our lambda function, we need to create the artifact first (jar).
`mvn clean install`

Now that we have the artifact in target folder, we can go ahead deploy it.
`serverless deploy --aws-profile serverlessUser`

You can check the AWS CloudFormation section in the Console to view details of the stack that has just been created.

# Invoking the function

We can invoke the function from the CLI, using the following command
`serverless invoke --function hello --aws-profile serverlessUser`

Additionally, we can pass an input (formatted for Powershell)
`serverless invoke --function hello --aws-profile serverlessUser --data '{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}'`

To check the generated logs:
`serverless logs --function hello --aws-profile serverlessUser`

And to see the function metrics:
`serverless metrics --function hello --aws-profile serverlessUser`


** Up to this point, we have created an AWS Lambda Function with Java. In the following steps we will create an API that we can expose with AWS API Gateway **


# Messaging API

We are going to update our current demo service to create a basic API with DynamoDB as the persistency layer, and make it available via Amazon API Gateway.

## Moving to IntelliJ

In IntelliJ, click Open, then select the pom.xml and pick Open as Project.

## Update Artifact
Change the pom.xml from

```
  <groupId>com.serverless</groupId>
  <artifactId>hello</artifactId>
  <packaging>jar</packaging>
  <version>dev</version>
  <name>hello</name>
```
to

```
  <groupId>com.serverless</groupId>
  <artifactId>messaging-api</artifactId>
  <packaging>jar</packaging>
  <version>dev</version>
  <name>messaging-api</name>
```

## Add DynamoDB
In order to introduce DynamoDB, add the Dependency in pom.xml:

```
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-java-sdk-dynamodb</artifactId>
  <version>1.11.119</version>
</dependency>
```

Serverless takes care of creating a basic lambda execution role, however, it only has CloudWatch Log permissions. So let us add permissions for DynamoDB in the `serverless.yml`, under `provider` section:

```
iamRoleStatements:
   - Effect: "Allow"
     Action:
       - "dynamodb:*"
     Resource: "*"
```

To create the DynamoDB Table we can insert a CloudFormation resource template at the end of the `serverless.yml` file:
```
resources:
  Resources:
    messagesTable:
      Type: AWS::DynamoDB::Table
      Properties:
        TableName: messages_table
        AttributeDefinitions:
          - AttributeName: user_id
            AttributeType: S
          - AttributeName: message_date
            AttributeType: S
        KeySchema:
          - AttributeName: user_id
            KeyType: HASH
          - AttributeName: message_date
            KeyType: RANGE
        ProvisionedThroughput:
          ReadCapacityUnits: 1
          WriteCapacityUnits: 1
```

### DynamoDB POJO and Adapter

The POJO will represent the DynamoDB Item, and the Adapter will help us to communicate with DynamoDB. The code is available in the project within this repository
- java-demo-project/src/main/java/com/serverless/data/Message.java
- java-demo-project/src/main/java/com/serverless/db/Adapter.java
- java-demo-project/src/main/java/com/serverless/db/DynamoAdapter.java

## Code the Handler functions

For this API we are going to use two Handlers: GetMessagesHandler and PostMessagesHandler, so we need to define them on the `serverless.yml` file:

```
  artifact: target/hello-dev.jar

functions:
  hello:
    handler: com.serverless.Handler
```

to
```
  artifact: target/messaging-api-dev.jar

functions:
  get-messages:
    handler: com.serverless.GetMessagesHandler
  post-message:
    handler: com.serverless.PostMessageHandler
```

You can find the code here for the previously created handlers within this repository:
- java-demo-project/src/main/java/com/serverless/GetMessagesHandler.java
- java-demo-project/src/main/java/com/serverless/PostMessageHandler.java

## Deploying the Functions

Re-create the artifact.
`mvn clean install`

Now that we have the artifact in target folder, we can go ahead deploy it.
`serverless deploy --aws-profile serverlessUser`

## Define API Endpoints

Now that we have our two functions deployed, we could use an API Gateway to trigger them. The Amazon API Gateway is a service that lets us expose many supported AWS offerings via RESTful API over HTTP. The API Gateway invocation is one of the many events supported by AWS Lambda; hence, we can define `http` as the event under the function definition in  `serverless.yml`. Update the handler section for the two functions:

```
get-messages:
    handler: com.serverless.GetMessagesHandler
    events:
      - http:
          path: /messaging/{user_id}/messages
          method: get

  post-message:
    handler: com.serverless.PostMessageHandler
    events:
      - http:
          path: /messaging/{user_id}/messages
          method: post
```

Re-deploy the Project
`mvn clean install`
`serverless deploy --aws-profile serverlessUser`

Once the deploy is completed, you will see the Endpoints for both methods (GET and POST).

## Testing the Endpoints

Use the Endpoints to test the services. Remember to replace the {user_id} with any mock data. For example:

*POST*
On cmd:
`curl -X POST  https://g6powi71l8.execute-api.us-east-1.amazonaws.com/dev/messaging/u123/messages -d "{\"message_id\":\"msg003\",\"text\":\"thirdattempt!\"}"`

On Powershell:
`Invoke-WebRequest https://g6powi71l8.execute-api.us-east-1.amazonaws.com/dev/messaging/u123/messages  -Method POST -Body '{"message_id":"msg001","text":"Hello from Powershell"}'`

*GET*

On cmd
`curl https://g6powi71l8.execute-api.us-east-1.amazonaws.com/dev/messaging/u123/messages`

On Powershell (Method and OutFile can be omitted):
`Invoke-WebRequest https://g6powi71l8.execute-api.us-east-1.amazonaws.com/dev/messaging/u123/messages -Method GET -OutFile response.txt`
