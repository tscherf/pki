// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cms.servlet.csadmin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.cert.CertificateEncodingException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.velocity.context.Context;
import org.mozilla.jss.ssl.SSLCertificateApprovalCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.netscape.certsrv.apps.CMS;
import com.netscape.certsrv.base.EBaseException;
import com.netscape.certsrv.base.IConfigStore;
import com.netscape.certsrv.property.PropertySet;
import com.netscape.cms.servlet.base.UserInfo;
import com.netscape.cms.servlet.wizard.IWizardPanel;
import com.netscape.cms.servlet.wizard.WizardServlet;
import com.netscape.cmsutil.crypto.CryptoUtil;
import com.netscape.cmsutil.xml.XMLObject;

public class WizardPanelBase implements IWizardPanel {
    public static String PCERT_PREFIX = "preop.cert.";
    public static String SUCCESS = "0";
    public static String FAILURE = "1";
    public static String AUTH_FAILURE = "2";

    /**
     * Definition for static variables in CS.cfg
     */
    public static final String CONF_CA_CERT = "ca.signing.cert";
    public static final String CONF_CA_CERTREQ = "ca.signing.certreq";
    public static final String CONF_CA_CERTNICKNAME = "ca.signing.certnickname";

    public static final String PRE_CONF_ADMIN_NAME = "preop.admin.name";
    public static final String PRE_CONF_AGENT_GROUP = "preop.admin.group";

    /**
     * Definition for "preop" static variables in CS.cfg
     * -- "preop" config parameters should not assumed to exist after configuation
     */

    public static final String PRE_CONF_CA_TOKEN = "preop.module.token";
    public static final String PRE_CA_TYPE = "preop.ca.type";
    public static final String PRE_OTHER_CA = "otherca";
    public static final String PRE_ROOT_CA = "rootca";

    private String mName = null;
    private int mPanelNo = 0;
    private String mId = null;

    /**
     * Initializes this panel.
     */
    public void init(ServletConfig config, int panelno)
            throws ServletException {
        mPanelNo = panelno;
    }

    public void init(WizardServlet servlet, ServletConfig config, int panelno, String id)
            throws ServletException {
        mPanelNo = panelno;
    }

    /**
     * Cleans up this panel so that isPanelDone() will return false.
     */
    public void cleanUp() throws IOException {
    }

    public String getName() {
        return mName;
    }

    public int getPanelNo() {
        return mPanelNo;
    }

    public void setPanelNo(int num) {
        mPanelNo = num;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public PropertySet getUsage() {
        PropertySet set = null;

        return set;
    }

    /**
     * Should we skip this panel?
     */
    public boolean shouldSkip() {
        return false;
    }

    /**
     * Is this panel done
     */
    public boolean isPanelDone() {
        return false;
    }

    /**
     * Show "Apply" button on frame?
     */
    public boolean showApplyButton() {
        return false;
    }

    /**
     * Is this a subPanel?
     */
    public boolean isSubPanel() {
        return false;
    }

    public boolean isLoopbackPanel() {
        return false;
    }

    /**
     * has subPanels?
     */
    public boolean hasSubPanel() {
        return false;
    }

    /**
     * Display the panel.
     */
    public void display(HttpServletRequest request,
            HttpServletResponse response,
            Context context) {
    }

    /**
     * Checks if the given parameters are valid.
     */
    public void validate(HttpServletRequest request,
            HttpServletResponse response,
            Context context) throws IOException {
    }

    /**
     * Commit parameter changes
     */
    public void update(HttpServletRequest request,
            HttpServletResponse response,
            Context context) throws IOException {
    }

    /**
     * If validiate() returns false, this method will be called.
     */
    public void displayError(HttpServletRequest request,
            HttpServletResponse response,
            Context context) {
    }

    /**
     * Retrieves locale based on the request.
     */
    public Locale getLocale(HttpServletRequest req) {
        Locale locale = null;
        String lang = req.getHeader("accept-language");

        if (lang == null) {
            // use server locale
            locale = Locale.getDefault();
        } else {
            locale = new Locale(UserInfo.getUserLanguage(lang),
                    UserInfo.getUserCountry(lang));
        }
        return locale;
    }

    public int getSubsystemCount(String hostname, int https_admin_port,
            boolean https, String type) throws IOException, SAXException, ParserConfigurationException {
        CMS.debug("WizardPanelBase getSubsystemCount start");
        String c = ConfigurationUtils.getDomainXML(hostname, https_admin_port, true);
        if (c != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(c.getBytes());
            XMLObject obj = new XMLObject(bis);
            String containerName = type + "List";
            Node n = obj.getContainer(containerName);
            NodeList nlist = n.getChildNodes();
            String countS = "";
            for (int i = 0; i < nlist.getLength(); i++) {
                Element nn = (Element) nlist.item(i);
                String tagname = nn.getTagName();
                if (tagname.equals("SubsystemCount")) {
                    NodeList nlist1 = nn.getChildNodes();
                    Node nn1 = nlist1.item(0);
                    countS = nn1.getNodeValue();
                    break;
                }
            }
            CMS.debug("WizardPanelBase getSubsystemCount: SubsystemCount=" + countS);
            int num = 0;

            if (countS != null && !countS.equals("")) {
                try {
                    num = Integer.parseInt(countS);
                } catch (Exception ee) {
                }
            }

            return num;
        }
        return -1;
    }

    public String getCertChainUsingSecureEEPort(String hostname,
                                                 int https_ee_port,
                                                 boolean https,
                                                 ConfigCertApprovalCallback
                                                 certApprovalCallback)
                                                 throws IOException {
        CMS.debug("WizardPanelBase getCertChainUsingSecureEEPort start");
        String c = ConfigurationUtils.getHttpResponse(hostname, https_ee_port, https,
                                    "/ca/ee/ca/getCertChain", null, null,
                                    certApprovalCallback);

        if (c != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(c.getBytes());
                XMLObject parser = null;

                try {
                    parser = new XMLObject(bis);
                } catch (Exception e) {
                    CMS.debug("WizardPanelBase::getCertChainUsingSecureEEPort() - "
                             + "Exception=" + e.toString());
                    throw new IOException(e.toString());
                }

                String status = parser.getValue("Status");

                CMS.debug("WizardPanelBase getCertChainUsingSecureEEPort: status=" + status);

                if (status.equals(SUCCESS)) {
                    String certchain = parser.getValue("ChainBase64");

                    certchain = CryptoUtil.normalizeCertStr(certchain);
                    CMS.debug(
                            "WizardPanelBase getCertChainUsingSecureEEPort: certchain="
                                    + certchain);
                    return certchain;
                } else {
                    String error = parser.getValue("Error");

                    throw new IOException(error);
                }
            } catch (IOException e) {
                CMS.debug("WizardPanelBase: getCertChainUsingSecureEEPort: " + e.toString());
                throw e;
            } catch (Exception e) {
                CMS.debug("WizardPanelBase: getCertChainUsingSecureEEPort: " + e.toString());
                throw new IOException(e.toString());
            }
        }

        return null;
    }

    public boolean updateConfigEntries(String hostname, int port, boolean https,
            String servlet, String uri, IConfigStore config,
            HttpServletResponse response) throws IOException {
        CMS.debug("WizardPanelBase updateConfigEntries start");
        String c = ConfigurationUtils.getHttpResponse(hostname, port, https, servlet, uri, null);

        if (c != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(c.getBytes());
                XMLObject parser = null;

                try {
                    parser = new XMLObject(bis);
                } catch (Exception e) {
                    CMS.debug("WizardPanelBase::updateConfigEntries() - "
                             + "Exception=" + e.toString());
                    throw new IOException(e.toString());
                }

                String status = parser.getValue("Status");

                CMS.debug("WizardPanelBase updateConfigEntries: status=" + status);

                if (status.equals(SUCCESS)) {
                    String cstype = "";
                    try {
                        cstype = config.getString("cs.type", "");
                    } catch (Exception e) {
                        CMS.debug("WizardPanelBase::updateConfigEntries() - unable to get cs.type: " + e.toString());
                    }

                    Document doc = parser.getDocument();
                    NodeList list = doc.getElementsByTagName("name");
                    int len = list.getLength();
                    for (int i = 0; i < len; i++) {
                        Node n = list.item(i);
                        NodeList nn = n.getChildNodes();
                        String name = nn.item(0).getNodeValue();
                        Node parent = n.getParentNode();
                        nn = parent.getChildNodes();
                        int len1 = nn.getLength();
                        String v = "";
                        for (int j = 0; j < len1; j++) {
                            Node nv = nn.item(j);
                            String val = nv.getNodeName();
                            if (val.equals("value")) {
                                NodeList n2 = nv.getChildNodes();
                                if (n2.getLength() > 0)
                                    v = n2.item(0).getNodeValue();
                                break;
                            }
                        }

                        if (name.equals("internaldb.basedn")) {
                            config.putString(name, v);
                            config.putString("preop.internaldb.master.basedn", v);
                        } else if (name.startsWith("internaldb")) {
                            config.putString(name.replaceFirst("internaldb", "preop.internaldb.master"), v);
                        } else if (name.equals("instanceId")) {
                            config.putString("preop.master.instanceId", v);
                        } else if (name.equals("cloning.cert.signing.nickname")) {
                            config.putString("preop.master.signing.nickname", v);
                            config.putString("preop.cert.signing.nickname", v);
                        } else if (name.equals("cloning.ocsp_signing.nickname")) {
                            config.putString("preop.master.ocsp_signing.nickname", v);
                            config.putString("preop.cert.ocsp_signing.nickname", v);
                        } else if (name.equals("cloning.subsystem.nickname")) {
                            config.putString("preop.master.subsystem.nickname", v);
                            config.putString("preop.cert.subsystem.nickname", v);
                        } else if (name.equals("cloning.transport.nickname")) {
                            config.putString("preop.master.transport.nickname", v);
                            config.putString("kra.transportUnit.nickName", v);
                            config.putString("preop.cert.transport.nickname", v);
                        } else if (name.equals("cloning.storage.nickname")) {
                            config.putString("preop.master.storage.nickname", v);
                            config.putString("kra.storageUnit.nickName", v);
                            config.putString("preop.cert.storage.nickname", v);
                        } else if (name.equals("cloning.audit_signing.nickname")) {
                            config.putString("preop.master.audit_signing.nickname", v);
                            config.putString("preop.cert.audit_signing.nickname", v);
                            config.putString(name, v);
                        } else if (name.startsWith("cloning.ca")) {
                            config.putString(name.replaceFirst("cloning", "preop"), v);
                        } else if (name.equals("cloning.signing.keyalgorithm")) {
                            config.putString(name.replaceFirst("cloning", "preop.cert"), v);
                            if (cstype.equals("CA")) {
                                config.putString("ca.crl.MasterCRL.signingAlgorithm", v);
                                config.putString("ca.signing.defaultSigningAlgorithm", v);
                            } else if (cstype.equals("OCSP")) {
                                config.putString("ocsp.signing.defaultSigningAlgorithm", v);
                            }
                        } else if (name.equals("cloning.transport.keyalgorithm")) {
                            config.putString(name.replaceFirst("cloning", "preop.cert"), v);
                            config.putString("kra.transportUnit.signingAlgorithm", v);
                        } else if (name.equals("cloning.ocsp_signing.keyalgorithm")) {
                            config.putString(name.replaceFirst("cloning", "preop.cert"), v);
                            if (cstype.equals("CA")) {
                                config.putString("ca.ocsp_signing.defaultSigningAlgorithm", v);
                            }
                        } else if (name.startsWith("cloning")) {
                            config.putString(name.replaceFirst("cloning", "preop.cert"), v);
                        } else {
                            config.putString(name, v);
                        }
                    }

                    // set master ldap password (if it exists) temporarily in password store
                    // in case it is needed for replication.  Not stored in password.conf.
                    try {
                        String master_pwd = config.getString("preop.internaldb.master.ldapauth.password", "");
                        if (!master_pwd.equals("")) {
                            config.putString("preop.internaldb.master.ldapauth.bindPWPrompt", "master_internaldb");
                            String passwordFile = config.getString("passwordFile");
                            IConfigStore psStore = CMS.createFileConfigStore(passwordFile);
                            psStore.putString("master_internaldb", master_pwd);
                            psStore.commit(false);
                        }
                    } catch (Exception e) {
                        CMS.debug("updateConfigEntries: Failed to temporarily store master bindpwd: " + e.toString());
                        e.printStackTrace();
                        throw new IOException(e.toString());
                    }

                    return true;
                } else if (status.equals(AUTH_FAILURE)) {
                    reloginSecurityDomain(response);
                    return false;
                } else {
                    String error = parser.getValue("Error");

                    throw new IOException(error);
                }
            } catch (IOException e) {
                CMS.debug("WizardPanelBase: updateConfigEntries: " + e.toString());
                throw e;
            } catch (Exception e) {
                CMS.debug("WizardPanelBase: updateConfigEntries: " + e.toString());
                throw new IOException(e.toString());
            }
        }

        return false;
    }

    public boolean authenticate(String hostname, int port, boolean https,
            String servlet, String uri) throws IOException {
        CMS.debug("WizardPanelBase authenticate start");
        String c = ConfigurationUtils.getHttpResponse(hostname, port, https, servlet, uri, null);
        IConfigStore cs = CMS.getConfigStore();

        if (c != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(c.getBytes());
                XMLObject parser = null;

                try {
                    parser = new XMLObject(bis);
                } catch (Exception e) {
                    CMS.debug("WizardPanelBase::authenticate() - "
                             + "Exception=" + e.toString());
                    throw new IOException(e.toString());
                }

                String status = parser.getValue("Status");

                CMS.debug("WizardPanelBase authenticate: status=" + status);

                if (status.equals(SUCCESS)) {
                    String cookie = parser.getValue("Cookie");
                    cs.putString("preop.cookie", cookie);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                CMS.debug("WizardPanelBase: authenticate: " + e.toString());
                throw new IOException(e.toString());
            }
        }

        return false;
    }

    public String pingCS(String hostname, int port, boolean https,
                          SSLCertificateApprovalCallback certApprovalCallback)
            throws IOException {
        CMS.debug("WizardPanelBase pingCS: started");

        String c = ConfigurationUtils.getHttpResponse(hostname, port, https,
                                    "/ca/admin/ca/getStatus",
                                    null, null, certApprovalCallback);

        if (c != null) {
            try {
                ByteArrayInputStream bis = new
                                           ByteArrayInputStream(c.getBytes());
                XMLObject parser = null;
                String state = null;

                try {
                    parser = new XMLObject(bis);
                    CMS.debug("WizardPanelBase pingCS: got XML parsed");
                    state = parser.getValue("State");

                    if (state != null) {
                        CMS.debug("WizardPanelBase pingCS: state=" + state);
                    }
                } catch (Exception e) {
                    CMS.debug("WizardPanelBase: pingCS: parser failed"
                             + e.toString());
                }

                return state;
            } catch (Exception e) {
                CMS.debug("WizardPanelBase: pingCS: " + e.toString());
                throw new IOException(e.toString());
            }
        }

        CMS.debug("WizardPanelBase pingCS: stopped");
        return null;
    }

    public void updateCertChainUsingSecureEEPort(IConfigStore config, String name, String host,
            int https_ee_port, boolean https, Context context, ConfigCertApprovalCallback certApprovalCallback)
            throws IOException, CertificateEncodingException, EBaseException {
        String certchain = getCertChainUsingSecureEEPort(host, https_ee_port, https, certApprovalCallback);
        config.putString("preop." + name + ".pkcs7", certchain);

        byte[] decoded = CryptoUtil.base64Decode(certchain);
        java.security.cert.X509Certificate[] b_certchain = CryptoUtil.getX509CertificateFromPKCS7(decoded);

        int size = 0;
        if (b_certchain != null) {
            size = b_certchain.length;
        }
        config.putInteger("preop." + name + ".certchain.size", size);

        for (int i = 0; i < size; i++) {
            byte[] bb = b_certchain[i].getEncoded();
            config.putString("preop." + name + ".certchain." + i,
                    CryptoUtil.normalizeCertStr(CryptoUtil.base64Encode(bb)));
        }

        config.commit(false);
    }

    public void reloginSecurityDomain(HttpServletResponse response) {
        IConfigStore cs = CMS.getConfigStore();
        try {
            String hostname = cs.getString("securitydomain.host", "");
            int port = cs.getInteger("securitydomain.httpsadminport", -1);
            String cs_hostname = cs.getString("machineName", "");
            int cs_port = cs.getInteger("pkicreate.admin_secure_port", -1);
            int panel = getPanelNo();
            String subsystem = cs.getString("cs.type", "");
            String urlVal =
                    "https://"
                            + cs_hostname + ":" + cs_port + "/" + subsystem.toLowerCase()
                            + "/admin/console/config/wizard?p=" + panel + "&subsystem=" + subsystem;
            String encodedValue = URLEncoder.encode(urlVal, "UTF-8");
            String sdurl = "https://" + hostname + ":" + port + "/ca/admin/ca/securityDomainLogin?url=" + encodedValue;
            response.sendRedirect(sdurl);
        } catch (Exception e) {
            CMS.debug("WizardPanelBase reloginSecurityDomain: Exception=" + e.toString());
        }
    }
}
