import os
import sys
import json
import subprocess

aws_region = sys.argv[1]
bucket_name = sys.argv[2]

stack_description_json_file = open('stack_description.json',mode='r')
stack_description_json = stack_description_json_file.read()
stack_description_json_file.close()
os.remove('./stack_description.json')

stack_description_dictionary = json.loads(stack_description_json)

result_file = open("../../../main/resources/config.json", 'w')

output_string = ("{\n"
"        \"accessKeyId\": \"" + stack_description_dictionary['Stacks'][0]['Outputs'][0]['OutputValue'] + "\",\n"
"        \"secretAccessKey\": \"" + stack_description_dictionary['Stacks'][0]['Outputs'][1]['OutputValue'] + "\",\n"
"        \"bucketName\": \"" + bucket_name + "\",\n"
"        \"region\": \"" + aws_region + "\"\n"                                             
"}")
result_file.write(output_string)
result_file.close()