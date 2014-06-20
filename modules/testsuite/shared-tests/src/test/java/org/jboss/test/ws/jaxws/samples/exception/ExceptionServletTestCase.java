/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

public class ExceptionServletTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-exception-client.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.helper.ClientHelper.class)
               .addClass(org.jboss.test.helper.TestServlet.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.ExceptionHelper.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.SOAP12ExceptionHelper.class)
               .addPackage("org.jboss.test.ws.jaxws.samples.exception.client");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-exception-jse.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.SOAP12EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.SOAP12ExceptionEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.UserException.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/exception/server/jaxws-handlers-server.xml")
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.UserExceptionBean.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(ExceptionServletTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   public void testRuntimeException() throws Exception
   {
      assertEquals("1", runTestInContainer("testRuntimeException"));
   }

   public void testSoapFaultException() throws Exception
   {
      assertEquals("1", runTestInContainer("testSoapFaultException"));
   }

   public void testApplicationException() throws Exception
   {
      assertEquals("1", runTestInContainer("testApplicationException"));
   }
   
   public void testRuntimeExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testRuntimeException"));
   }

   public void testSoapFaultExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testSoapFaultException"));
   }

   public void testApplicationExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testApplicationException"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/ExceptionEndpointService&method=" + test
            + "&helper=" + ExceptionHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
   
   private String runTestInContainerSOAP12(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/SOAP12ExceptionEndpointService&method=" + test
            + "&helper=" + SOAP12ExceptionHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
