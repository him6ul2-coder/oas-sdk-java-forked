package egain.oassdk.dev.sla;

import egain.oassdk.core.exceptions.GenerationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Generates SLA scripts for API Gateway
 */
public class SLAGatewayScripts {

    /**
     * Generate SLA gateway scripts
     *
     * @param openApiSpec OpenAPI specification
     * @param slaSpec     SLA specification
     * @param outputDir   Output directory
     * @throws GenerationException if generation fails
     */
    public void generateScripts(Map<String, Object> openApiSpec, Map<String, Object> slaSpec, String outputDir) throws GenerationException {
        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory cannot be null");
        }
        try {
            Files.createDirectories(Paths.get(outputDir));

            // Generate AWS API Gateway scripts
            generateAWSScripts(openApiSpec, slaSpec, outputDir + "/aws");

            // Generate Kong scripts
            generateKongScripts(openApiSpec, slaSpec, outputDir + "/kong");

            // Generate NGINX scripts
            generateNginxScripts(openApiSpec, slaSpec, outputDir + "/nginx");

            // Generate Envoy scripts
            generateEnvoyScripts(openApiSpec, slaSpec, outputDir + "/envoy");

            // Generate Istio scripts
            generateIstioScripts(openApiSpec, slaSpec, outputDir + "/istio");

        } catch (Exception e) {
            throw new GenerationException("Failed to generate SLA gateway scripts: " + e.getMessage(), e);
        }
    }

    /**
     * Generate AWS API Gateway scripts
     */
    private void generateAWSScripts(Map<String, Object> openApiSpec, Map<String, Object> slaSpec, String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        // Generate API Gateway policy
        String policy = generateAWSPolicy(openApiSpec, slaSpec);
        Files.write(Paths.get(outputDir, "api-gateway-policy.json"), policy.getBytes(StandardCharsets.UTF_8));

        // Generate Lambda authorizer
        String lambdaAuthorizer = generateAWSLambdaAuthorizer(openApiSpec, slaSpec);
        Files.write(Paths.get(outputDir, "lambda-authorizer.js"), lambdaAuthorizer.getBytes(StandardCharsets.UTF_8));

        // Generate CloudFormation template
        String cloudFormation = generateAWSCloudFormation(openApiSpec, slaSpec);
        Files.write(Paths.get(outputDir, "cloudformation-template.yaml"), cloudFormation.getBytes(StandardCharsets.UTF_8));

        // Generate Terraform configuration
        String terraform = generateAWSTerraform(openApiSpec, slaSpec);
        Files.write(Paths.get(outputDir, "main.tf"), terraform.getBytes(StandardCharsets.UTF_8));

        // Generate deployment script
        String deployScript = generateAWSDeployScript(openApiSpec, slaSpec);
        Files.write(Paths.get(outputDir, "deploy.sh"), deployScript.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate AWS API Gateway policy
     */
    private String generateAWSPolicy(Map<String, Object> openApiSpec, Map<String, Object> slaSpec) {
        return """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": "execute-api:Invoke",
                            "Resource": "arn:aws:execute-api:*:*:*/*/*",
                            "Condition": {
                                "StringEquals": {
                                    "aws:SourceIp": "0.0.0.0/0"
                                }
                            }
                        }
                    ]
                }
                """;
    }

    /**
     * Generate AWS Lambda authorizer
     */
    private String generateAWSLambdaAuthorizer(Map<String, Object> openApiSpec, Map<String, Object> slaSpec) {
        return """
                exports.handler = async (event) => {
                    const token = event.authorizationToken;
                    const methodArn = event.methodArn;
                
                    // Validate token
                    if (!token || token !== 'valid-token') {
                        throw new Error('Unauthorized');
                    }
                
                    // Generate policy
                    const policy = generatePolicy('user', 'Allow', methodArn);
                
                    return policy;
                };
                
                function generatePolicy(principalId, effect, resource) {
                    const authResponse = {};
                    authResponse.principalId = principalId;
                
                    if (effect && resource) {
                        const policyDocument = {};
                        policyDocument.Version = '2012-10-17';
                        policyDocument.Statement = [];
                        const statementOne = {};
                        statementOne.Action = 'execute-api:Invoke';
                        statementOne.Effect = effect;
                        statementOne.Resource = resource;
                        policyDocument.Statement[0] = statementOne;
                        authResponse.policyDocument = policyDocument;
                    }
                
                    return authResponse;
                }
                """;
    }

    /**
     * Generate AWS CloudFormation template
     */
    private String generateAWSCloudFormation(Map<String, Object> openApiSpec, Map<String, Object> slaSpec) {
        return """
                AWSTemplateFormatVersion: '2010-09-09'
                Transform: AWS::Serverless-2016-10-31
                
                Parameters:
                  StageName:
                    Type: String
                    Default: dev
                    Description: Stage name for the API
                
                Resources:
                  ApiGateway:
                    Type: AWS::Serverless::Api
                    Properties:
                      StageName: !Ref StageName
                      Cors:
                        AllowMethods: "'GET,POST,PUT,DELETE,OPTIONS'"
                        AllowHeaders: "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
                        AllowOrigin: "'*'"
                      Auth:
                        DefaultAuthorizer: LambdaTokenAuthorizer
                        Authorizers:
                          LambdaTokenAuthorizer:
                            FunctionArn: !GetAtt LambdaAuthorizer.Arn
                      DefinitionBody:
                        Fn::Transform:
                          Name: AWS::Include
                          Parameters:
                            Location: openapi.yaml
                
                  LambdaAuthorizer:
                    Type: AWS::Serverless::Function
                    Properties:
                      CodeUri: lambda-authorizer.js
                      Handler: index.handler
                      Runtime: nodejs18.x
                      Policies:
                        - Version: '2012-10-17'
                          Statement:
                            - Effect: Allow
                              Action:
                                - execute-api:Invoke
                              Resource: '*'
                
                Outputs:
                  ApiUrl:
                    Description: API Gateway endpoint URL
                    Value: !Sub 'https://${ApiGateway}.execute-api.${AWS::Region}.amazonaws.com/${StageName}'
                """;
    }

    /**
     * Generate AWS Terraform configuration
     */
    private String generateAWSTerraform(Map<String, Object> openApiSpec, Map<String, Object> slaSpec) {
        return """
                terraform {
                  required_providers {
                    aws = {
                      source  = "hashicorp/aws"
                      version = "~> 5.0"
                    }
                  }
                }
                
                provider "aws" {
                  region = var.aws_region
                }
                
                variable "aws_region" {
                  description = "AWS region"
                  type        = string
                  default     = "us-east-1"
                }
                
                variable "stage_name" {
                  description = "API Gateway stage name"
                  type        = string
                  default     = "dev"
                }
                
                resource "aws_api_gateway_rest_api" "api" {
                  name        = "sla-api"
                  description = "API with SLA enforcement"
                
                  endpoint_configuration {
                    types = ["REGIONAL"]
                  }
                }
                
                resource "aws_api_gateway_deployment" "deployment" {
                  rest_api_id = aws_api_gateway_rest_api.api.id
                  stage_name  = var.stage_name
                
                  depends_on = [
                    aws_api_gateway_method.method,
                    aws_api_gateway_integration.integration
                  ]
                }
                
                resource "aws_api_gateway_resource" "resource" {
                  rest_api_id = aws_api_gateway_rest_api.api.id
                  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
                  path_part   = "api"
                }
                
                resource "aws_api_gateway_method" "method" {
                  rest_api_id   = aws_api_gateway_rest_api.api.id
                  resource_id   = aws_api_gateway_resource.resource.id
                  http_method   = "ANY"
                  authorization = "NONE"
                }
                
                resource "aws_api_gateway_integration" "integration" {
                  rest_api_id = aws_api_gateway_rest_api.api.id
                  resource_id = aws_api_gateway_resource.resource.id
                  http_method = aws_api_gateway_method.method.http_method
                
                  type = "HTTP_PROXY"
                  uri  = "http://example.com"
                }
                
                output "api_url" {
                  value = "https://${aws_api_gateway_rest_api.api.id}.execute-api.${var.aws_region}.amazonaws.com/${var.stage_name}"
                }
                """;
    }

    /**
     * Generate AWS deployment script
     */
    private String generateAWSDeployScript(Map<String, Object> openApiSpec, Map<String, Object> slaSpec) {
        return """
                #!/bin/bash
                
                set -e
                
                echo "Deploying AWS API Gateway with SLA enforcement..."
                
                # Check if AWS CLI is installed
                if ! command -v aws &> /dev/null; then
                    echo "AWS CLI is not installed. Please install it first."
                    exit 1
                fi
                
                # Check if Terraform is installed
                if ! command -v terraform &> /dev/null; then
                    echo "Terraform is not installed. Please install it first."
                    exit 1
                fi
                
                # Initialize Terraform
                echo "Initializing Terraform..."
                terraform init
                
                # Plan deployment
                echo "Planning deployment..."
                terraform plan -out=tfplan
                
                # Apply deployment
                echo "Applying deployment..."
                terraform apply tfplan
                
                # Get API URL
                API_URL=$(terraform output -raw api_url)
                echo "API deployed at: $API_URL"
                
                # Test API
                echo "Testing API..."
                curl -X GET "$API_URL/api/health" || echo "API test failed"
                
                echo "Deployment completed successfully!"
                """;
    }

    /**
     * Generate Kong scripts
     */
    private void generateKongScripts(Map<String, Object> openApiSpec, Map<String, Object> slaSpec, String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        String kongConfig = """
                _format_version: "3.0"
                
                services:
                - name: api-service
                  url: http://api-service:8080
                  routes:
                  - name: api-route
                    paths:
                    - /
                  plugins:
                  - name: rate-limiting
                    config:
                      minute: 1000
                      hour: 10000
                      policy: local
                  - name: response-transformer
                    config:
                      add:
                        headers:
                        - "X-Response-Time:$(latency_ms)"
                  - name: prometheus
                    config:
                      per_consumer: true
                """;

        Files.write(Paths.get(outputDir, "kong.yml"), kongConfig.getBytes(StandardCharsets.UTF_8));

        String dockerCompose = """
                version: '3.8'
                
                services:
                  kong:
                    image: kong:latest
                    ports:
                      - "8000:8000"
                      - "8001:8001"
                    environment:
                      KONG_DATABASE: "off"
                      KONG_DECLARATIVE_CONFIG: /var/lib/kong/kong.yml
                    volumes:
                      - ./kong.yml:/var/lib/kong/kong.yml
                    command: kong start --run-migrations
                """;

        Files.write(Paths.get(outputDir, "docker-compose.yml"), dockerCompose.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate NGINX scripts
     */
    private void generateNginxScripts(Map<String, Object> openApiSpec, Map<String, Object> slaSpec, String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        String nginxConfig = """
                upstream api_backend {
                    server api-service:8080;
                }
                
                server {
                    listen 80;
                    server_name localhost;
                
                    # Rate limiting
                    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
                
                    location / {
                        limit_req zone=api burst=20 nodelay;
                
                        proxy_pass http://api_backend;
                        proxy_set_header Host $host;
                        proxy_set_header X-Real-IP $remote_addr;
                        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                        proxy_set_header X-Forwarded-Proto $scheme;
                
                        # Add response time header
                        add_header X-Response-Time $request_time;
                    }
                
                    # Health check endpoint
                    location /health {
                        access_log off;
                        return 200 "healthy\\n";
                        add_header Content-Type text/plain;
                    }
                }
                """;

        Files.write(Paths.get(outputDir, "nginx.conf"), nginxConfig.getBytes(StandardCharsets.UTF_8));

        String dockerfile = """
                FROM nginx:alpine
                
                COPY nginx.conf /etc/nginx/nginx.conf
                
                EXPOSE 80
                
                CMD ["nginx", "-g", "daemon off;"]
                """;

        Files.write(Paths.get(outputDir, "Dockerfile"), dockerfile.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate Envoy scripts
     */
    private void generateEnvoyScripts(Map<String, Object> openApiSpec, Map<String, Object> slaSpec, String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        String envoyConfig = """
                static_resources:
                  listeners:
                  - name: listener_0
                    address:
                      socket_address:
                        address: 0.0.0.0
                        port_value: 8080
                    filter_chains:
                    - filters:
                      - name: envoy.filters.network.http_connection_manager
                        typed_config:
                          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                          stat_prefix: ingress_http
                          route_config:
                            name: local_route
                            virtual_hosts:
                            - name: local_service
                              domains: ["*"]
                              routes:
                              - match:
                                  prefix: "/"
                                route:
                                  cluster: api_service
                          http_filters:
                          - name: envoy.filters.http.ratelimit
                            typed_config:
                              "@type": type.googleapis.com/envoy.extensions.filters.http.ratelimit.v3.RateLimit
                              domain: api
                              rate_limit_service:
                                grpc_service:
                                  envoy_grpc:
                                    cluster_name: rate_limit_service
                          - name: envoy.filters.http.router
                            typed_config:
                              "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
                  clusters:
                  - name: api_service
                    connect_timeout: 0.25s
                    type: LOGICAL_DNS
                    lb_policy: ROUND_ROBIN
                    load_assignment:
                      cluster_name: api_service
                      endpoints:
                      - lb_endpoints:
                        - endpoint:
                            address:
                              socket_address:
                                address: api-service
                                port_value: 8080
                """;

        Files.write(Paths.get(outputDir, "envoy.yaml"), envoyConfig.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate Istio scripts
     */
    private void generateIstioScripts(Map<String, Object> openApiSpec, Map<String, Object> slaSpec, String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        String virtualService = """
                apiVersion: networking.istio.io/v1alpha3
                kind: VirtualService
                metadata:
                  name: api-service
                spec:
                  hosts:
                  - api-service
                  http:
                  - match:
                    - uri:
                        prefix: /
                    route:
                    - destination:
                        host: api-service
                        port:
                          number: 8080
                    fault:
                      delay:
                        percentage:
                          value: 0.1
                        fixedDelay: 5s
                """;

        Files.write(Paths.get(outputDir, "virtual-service.yaml"), virtualService.getBytes(StandardCharsets.UTF_8));

        String destinationRule = """
                apiVersion: networking.istio.io/v1alpha3
                kind: DestinationRule
                metadata:
                  name: api-service
                spec:
                  host: api-service
                  trafficPolicy:
                    connectionPool:
                      tcp:
                        maxConnections: 100
                      http:
                        http1MaxPendingRequests: 10
                        maxRequestsPerConnection: 2
                    circuitBreaker:
                      consecutiveErrors: 3
                      interval: 30s
                      baseEjectionTime: 30s
                """;

        Files.write(Paths.get(outputDir, "destination-rule.yaml"), destinationRule.getBytes(StandardCharsets.UTF_8));
    }
}
