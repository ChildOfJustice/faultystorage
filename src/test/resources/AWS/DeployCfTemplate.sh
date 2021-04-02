#!/bin/bash
aws cloudformation deploy \
 	--template-file TestCloudFormationTemplate.yml \
 	--stack-name $1 \
 	--capabilities CAPABILITY_NAMED_IAM \
 	--parameter-overrides \
 	S3BucketName=$2 \
 	--region $3