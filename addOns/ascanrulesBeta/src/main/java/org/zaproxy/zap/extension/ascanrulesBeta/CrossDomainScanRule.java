/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.ascanrulesBeta;

import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.commonlib.CommonAlertTag;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Map;

//import org.parosproxy.paros.core.scanner.AbstractHostPlugin;




// import org.apache.http.impl.client.HttpClients;

/**
 * A class to actively check if the web server is configured to allow Cross Domain access, from a
 * malicious third party service, for instance. Currently checks for wildcards in Adobe's
 * crossdomain.xml, and in SilverLight's clientaccesspolicy.xml
 *
 * @author 70pointer@gmail.com
 */
public class CrossDomainScanRule extends AbstractAppParamPlugin {

    /** the logger object */
    private static Logger log = LogManager.getLogger(CrossDomainScanRule.class);

    /** Prefix for internationalized messages used by this rule */
    private static final String MESSAGE_PREFIX = "ascanbeta.crossdomain.";
// rami code
    private static String evilCors = "Access-Control-Allow-Origin: evil.com";

    private static final String MESSAGE_PREFIX_CORS = "ascanbeta.crossdomain.cors.";


    //rami code
    private static final String MESSAGE_PREFIX_ADOBE = "ascanbeta.crossdomain.adobe.";
    private static final String MESSAGE_PREFIX_ADOBE_READ = "ascanbeta.crossdomain.adobe.read.";
    private static final String MESSAGE_PREFIX_ADOBE_SEND = "ascanbeta.crossdomain.adobe.send.";
    private static final String MESSAGE_PREFIX_SILVERLIGHT = "ascanbeta.crossdomain.silverlight.";

    /** Adobe's cross domain policy file name */
    static final String ADOBE_CROSS_DOMAIN_POLICY_FILE = "crossdomain.xml";

    /** Silverlight's cross domain policy file name */
    static final String SILVERLIGHT_CROSS_DOMAIN_POLICY_FILE = "clientaccesspolicy.xml";

    private static final Map<String, String> ALERT_TAGS =
            CommonAlertTag.toMap(
                    CommonAlertTag.OWASP_2021_A05_SEC_MISCONFIG,
                    CommonAlertTag.OWASP_2017_A06_SEC_MISCONFIG);

    private DocumentBuilder docBuilder;
    private XPath xpath;

    @Override
    public int getId() {
        return 20016;
    }

    @Override
    public String getName() {
        return Constant.messages.getString(MESSAGE_PREFIX + "name");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public int getCategory() {
        return Category.SERVER;
    }

    @Override
    public String getSolution() {
        return "";
    }

    @Override
    public String getReference() {
        return Constant.messages.getString(MESSAGE_PREFIX + "refs");
    }

    @Override
    public void init() {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            docBuilderFactory.setFeature(
                    "http://xml.org/sax/features/external-general-entities", false);
            docBuilderFactory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities", false);
            docBuilderFactory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            docBuilderFactory.setExpandEntityReferences(false);
            docBuilder = docBuilderFactory.newDocumentBuilder();
            xpath = XPathFactory.newInstance().newXPath();
        } catch (ParserConfigurationException e) {
            log.error("Failed to create document builder:", e);
        }
    }

    /** scans the node for cross-domain mis-configurations */
    @Override
    public void scan() {
        if (docBuilder == null) {
            return;
        }

        try {
            // get the network details for the attack
            URI originalURI = this.getBaseMsg().getRequestHeader().getURI();

         //   scanAdobeCrossdomainPolicyFile(originalURI);

           // scanSilverlightCrossdomainPolicyFile(originalURI);

            scanCORS(originalURI);

        } catch (Exception e) {
            // needed to catch exceptions from the "finally" statement
            log.error(
                    "Error scanning a node for Cross Domain misconfigurations: {}",
                    e.getMessage(),
                    e);
        }
    }

    private void scanCORS(URI origURI)  throws IOException {

	    log.debug("Rami new Function"); 
        String s1 = origURI.toString();
        HttpMessage msg = getNewMsg();
        setParameter(msg, "Origin", "evil.com");

        // Send the request and retrieve the response
        try {
            sendAndReceive(msg, false);
        } catch (IOException ex) {
            log.debug(
                    "Caught {}{} when accessing: {}",
                    ex.getClass().getName(),
                    ex.getMessage(),
                    msg.getRequestHeader().getURI());
            return;
        }

        if(msg.getResponseHeader().toString().contains(evilCors)){

            newAlert()
                    .setConfidence(Alert.CONFIDENCE_MEDIUM)
                    .setName(
                            Constant.messages.getString(MESSAGE_PREFIX_CORS + "name"))
                    .setDescription(
                            Constant.messages.getString(MESSAGE_PREFIX_CORS + "desc"))
                    .setOtherInfo(
                            Constant.messages.getString(
                                    MESSAGE_PREFIX_CORS + "extrainfo",
                                    "/" + MESSAGE_PREFIX_CORS))
                    .setEvidence("<allow-access-from any domain=\"*\"")
                    .setMessage(msg)
                    .raise();

        }

          }






    @Override
    public int getRisk() {
        return Alert.RISK_HIGH;
    }

    @Override
    public int getCweId() {
        return 264; // CWE 264: Permissions, Privileges, and Access Controls
        // the more specific CWE's under this one are not rally relevant
    }

    @Override
    public int getWascId() {
        return 14; // WASC-14: Server Misconfiguration
    }

    @Override
    public Map<String, String> getAlertTags() {
        return ALERT_TAGS;
    }
}
