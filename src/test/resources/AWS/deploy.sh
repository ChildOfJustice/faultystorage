#!/bin/bash
S3StorageBucketName="jetbrains-sardor-bucket"
AwsRegion="eu-central-1"
StackName="JetBrainsInternshipStack"

echo "STEP 1 #### Deploying CloudFormation stack...";
sh DeployCfTemplate.sh $StackName $S3StorageBucketName $AwsRegion &&

echo "STEP 2 #### generating config.json...";
rm "../../../main/resources/config.json"
aws cloudformation describe-stacks --stack-name $StackName > "stack_description.json" &&
python3 ./generate_config_script.py $AwsRegion $S3StorageBucketName