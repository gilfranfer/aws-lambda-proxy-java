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
5. Select "Create Group", provide a name, and select the following Policies: AmazonS3FullAccess, CloudWatchLogsFullAccess, IAMFullAccess.
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
