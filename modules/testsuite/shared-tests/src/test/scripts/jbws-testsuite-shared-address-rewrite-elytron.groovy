def root = new XmlParser().parse(inputFile)

/**
 * Fix logging: optionally remove CONSOLE handler and set a specific log file
 *
 */
def logHandlers = root.profile.subsystem.'root-logger'.handlers[0]
def consoleHandler = logHandlers.find{it.@name == 'CONSOLE'}
if (!project.properties['enableServerLoggingToConsole']) logHandlers.remove(consoleHandler)
def file = root.profile.subsystem.'periodic-rotating-file-handler'.file[0]
file.attributes()['path'] = serverLog


/**
 * Add a security-domain block like this:
 *
 *        <subsystem xmlns="urn:wildfly:elytron:1.0">
 *           <security-domains>
 *                <security-domain name="JBossWS" default-realm="JBossWS" permission-mapper="login-permission-mapper" role-mapper="combined-role-mapper">
 *                   <realm name="JBossWS" role-decoder="groups-to-roles"/>
 *               </security-domain>
 *           </security-domains>
 * 
 *
 */
def subsystems = root.profile.subsystem
def securitySubsystem = null
def securityDomains = null
for (item in subsystems) {
    if (item.name().getNamespaceURI().contains("urn:wildfly:elytron:")) {
       securitySubsystem = item
       for (element in item) {
           if (element.name().getLocalPart() == 'security-domains') {
              securityDomains = element
           }
       }
       break
    }
}
def securityDomain = securityDomains.appendNode('security-domain', ['name':'JBossWS','default-realm':'JBossWS','permission-mapper':'default-permission-mapper'])
def realm = securityDomain.appendNode('realm',['name':'JBossWS','role-decoder':'groups-to-roles'])
/**
 *  <security-realms>
 *     <properties-realm name="JBossWS">
 *        <users-properties path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *        <groups-properties path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-roles.properties"/>
 *     </properties-realm>
*/
def securityRealms = root.profile.subsystem.'security-realms'[0]
def propertiesRealm = securityRealms.appendNode('properties-realm', ['name':'JBossWS'])
def usersProperties = propertiesRealm.appendNode('users-properties',['path':usersPropFile, 'plain-text':'true'])
def groupsProperties = propertiesRealm.appendNode('groups-properties',['path':rolesPropFile])

 /*   <http>
 *      <http-authentication-factory name="JBossWS" http-server-mechanism-factory="global" security-domain="JBossWS">
 *         <mechanism-configuration>
 *             <mechanism mechanism-name="BASIC">
 *                 <mechanism-realm realm-name="JBossWS Realm"/>
 *             </mechanism>
 *     </mechanism-configuration>
 */
def httpAuthen = null
for (element in securitySubsystem) {
    if (element.name().getLocalPart() == 'http') {
       httpAuthen = element
       break
    }
}
def httpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'JBossWS','http-server-mechanism-factory':'global', 'security-domain':'JBossWS'])
def mechanismConfiguration = httpAuthenticationFactory.appendNode('mechanism-configuration')
def mechanism = mechanismConfiguration.appendNode('mechanism',['mechanism-name':'BASIC'])
def mechanismRealm=mechanism.appendNode('mechanism-realm',['realm-name':'JBossWS'])

/**
 * Add a https connector like this:
 *
 * <security-realm name="jbws-test-https-realm">
 *    <server-identities>
 *        <ssl>
 *             <keystore path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/test.keystore" keystore-password="changeit" alias="tomcat"/>
 *        </ssl>
 *    </server-identities>
 * </security-realm>
 *
 */

def secRealms = root.management.'security-realms'[0]
def secRealm = secRealms.appendNode('security-realm', ['name':'jbws-test-https-realm'])
def serverIdentities = secRealm.appendNode('server-identities')
def ssl = serverIdentities.appendNode('ssl')
ssl.appendNode('keystore', ['path':keystorePath,'keystore-password':'changeit','alias':'tomcat'])

def server = root.profile.subsystem.server[0]
def curHttpsListener = server.'https-listener'[0]
if (curHttpsListener != null) server.remove(curHttpsListener)
server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(outputFile)
f.write(writer.toString())
