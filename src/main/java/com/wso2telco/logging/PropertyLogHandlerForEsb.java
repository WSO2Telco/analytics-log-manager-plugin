/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under  the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wso2telco.logging;

import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.XML;

import java.util.TreeMap;

public class PropertyLogHandlerForEsb extends AbstractMediator {

    private static final String REGISTRY_PATH = "gov:/event/";
    private static final String MESSAGE_TYPE = "message.type";
    private static final String PAYLOAD_LOGGING_ENABLED = "payload.logging.enabled";
    private static final String APPLICATION_ID = "APPLICATION_ID";
    private static final String API_PUBLISHER = "API_PUBLISHER";
    private static final String API_NAME = "API_NAME";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String USER_ID = "USER_ID";
    private static final String API_VERSION = "API_VERSION";
    private static final String CONTEXT = "CONTEXT";
    private static final String APPLICATION_NAME = "APPLICATION_NAME";
    private static final String CONSUMER_KEY = "CONSUMER_KEY";
    private static final String RESOURCE = "RESOURCE";
    private static final String HTTP_METHOD = "HTTP_METHOD";
    private static final String ERRVAR = "errvar";
    private static final String REQUEST = "request";
    private static final String RESPONSE = "response";
    private static final String UUID = "MESSAGE_ID";
    private static final String ERROR = "error";
    private static final String ERROR_EXCEPTION = "ERROR_EXCEPTION";
    private static final String API_RESOURCE_CACHE_KEY = "API_RESOURCE_CACHE_KEY";
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String HTTP_SC = "HTTP_SC";
    private static final String RESPONSE_TIME = "RESPONSE_TIME";
    private static final String CONTENT_TYPE = "messageType";

    private static final Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");

    public boolean mediate(MessageContext messageContext) {
        boolean isPayloadLoggingEnabled = false;

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        isPayloadLoggingEnabled = extractPayloadLoggingStatus(messageContext);
        String direction = (String) axis2MessageContext.getProperty(MESSAGE_TYPE);
		if (direction != null && direction.equalsIgnoreCase(REQUEST)) {
        	logRequestProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled);
        } else if (direction != null && direction.equalsIgnoreCase(RESPONSE)) {
            logResponseProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled);
        } else if (direction != null && direction.equalsIgnoreCase(ERROR)) {
            logErrorProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled);
        }

        return true;
    }

    private void logRequestProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled) {
        TreeMap<String, String> headers = (TreeMap<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (isPayloadLoggingEnabled) {
            String requestPayload = handleAndReturnPayload(messageContext);
            logHandler.info("TRANSACTION:request" +
					//",API_REQUEST_ID:" + messageContext.getProperty(UUID) + "" +
					",API_REQUEST_ID:" + headers.get(REQUEST_ID) + "" +
                    ",API_NAME:" + headers.get(API_NAME) + "" +
                    ",SP_NAME:" + headers.get(USER_ID) + "" +
                    ",API_PUBLISHER:" + headers.get(API_PUBLISHER) + "" +
                    ",API_VERSION:" + headers.get(API_VERSION) + "" +
                    ",API_CONTEXT:" + headers.get(CONTEXT) + "" +
                    ",APPLICATION_NAME:" + headers.get(APPLICATION_NAME) + "" +
                    ",APPLICATION_ID:" + (String) headers.get(APPLICATION_ID) + "" +
                    ",CONSUMER_KEY:" + headers.get(CONSUMER_KEY) + "" +
                    ",API_RESOURCE_PATH:" + headers.get(RESOURCE) + "" +
                    ",METHOD:" + headers.get(HTTP_METHOD) +
                    ",BODY:" + requestPayload.replaceAll("\n", ""));
        }
    }

    private void logResponseProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled) {
        if (isPayloadLoggingEnabled) {
            String responsePayload = handleAndReturnPayload(messageContext);
            logHandler.info("TRANSACTION:response" +
					",API_REQUEST_ID:" + messageContext.getProperty(REQUEST_ID) + "" +
                    ",HTTP_STATUS:" + axis2MessageContext.getProperty(HTTP_SC) + "" +
                    ",RESPONSE_TIME:" + messageContext.getProperty(RESPONSE_TIME) + "" +
                    ",BODY:" + responsePayload.replaceAll("\n", ""));
        }
    }

    private void logErrorProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled) {
        //UniqueIDGenerator.generateAndSetUniqueID("EX", axis2MessageContext);
        TreeMap<String, String> headers = (TreeMap<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (isPayloadLoggingEnabled) {
            logHandler.info("TRANSACTION:errorResponse," +
					//mife.prop.requestId set in UniqueIDGenerator
                    //",API_REQUEST_ID:" + axis2MessageContext.getProperty(REQUEST_ID) +
                    "API_REQUEST_ID:" + messageContext.getProperty(REQUEST_ID) +
                    ",REQUEST_BODY:" + messageContext.getEnvelope().getBody().toString() +
                    ",REST_FULL_REQUEST_PATH:" + headers.get(RESOURCE) +
                    ",SYNAPSE_REST_API:" + headers.get(API_NAME) +
                    ",SYNAPSE_REST_API_VERSION:" + headers.get(API_VERSION) +
                    ",API_RESOURCE_CACHE_KEY:" + messageContext.getProperty(API_RESOURCE_CACHE_KEY) + //Not available
                    ",ERROR_EXCEPTION:" + messageContext.getProperty(ERROR_EXCEPTION) + //Not available
                    ",APPLICATION_NAME:" + headers.get(APPLICATION_NAME) +
                    ",APPLICATION_ID:" + (String) headers.get(APPLICATION_ID) +
                    ",ERROR_CODE:" + messageContext.getProperty(ERROR_CODE) +
                    ",HTTP_STATUS:" + axis2MessageContext.getProperty(HTTP_SC) + "" +
                    ",ERROR_MESSAGE:" + messageContext.getProperty(ERRVAR));
        }
    }

    private boolean extractPayloadLoggingStatus(MessageContext messageContext) {
        boolean isPayloadLoggingEnabled = false;

        Entry payloadEntry = new Entry(REGISTRY_PATH + PAYLOAD_LOGGING_ENABLED);

        OMTextImpl payloadEnableRegistryValue = (OMTextImpl) messageContext.getConfiguration().getRegistry()
                .getResource(payloadEntry, null);

        if (payloadEnableRegistryValue != null) {
            String payloadLogEnabled = payloadEnableRegistryValue.getText();

            if (nullOrTrimmed(payloadLogEnabled) != null) {
                isPayloadLoggingEnabled = Boolean.valueOf(payloadLogEnabled);
            }
        }

        return isPayloadLoggingEnabled;
    }

    private static String nullOrTrimmed(String inputString) {
        String result = null;
        if (inputString != null && inputString.trim().length() > 0) {
            result = inputString.trim();
        }
        return result;
    }

	/**
	 * method used to handle invalid payloads
	 */
	private String handleAndReturnPayload(MessageContext messageContext) {
		String payload = "";
		try {
			payload = messageContext.getEnvelope().getBody().toString();
		} catch (Exception e) {
			payload = "payload dropped due to invalid format";
		} finally {
			return payload;
		}
	}

    /** this method can be used if we need to get extract only json as body**/
    private String getPayloadSting(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext) {
        String payload;
        if (axis2MessageContext.getProperty(CONTENT_TYPE).equals("application/json")) {
            /**if content type is json */
            payload = XML.toJSONObject(messageContext.getEnvelope().getBody().getFirstElement().getFirstElement().toString()).toString();
        } else if (axis2MessageContext.getProperty(CONTENT_TYPE).equals("text/plain")) {
            /**if content type is text/plain */
            payload = messageContext.getEnvelope().getBody().getFirstElement().toString();
        } else {
            /** if content type is xml */
            payload = messageContext.getEnvelope().getBody().toString();
        }
        return payload;
    }
}