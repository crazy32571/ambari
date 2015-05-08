/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.checks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.ambari.server.controller.PrereqCheckRequest;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.Config;
import org.apache.ambari.server.state.Service;
import org.apache.ambari.server.state.StackId;
import org.apache.ambari.server.state.stack.PrereqCheckStatus;
import org.apache.ambari.server.state.stack.PrerequisiteCheck;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Provider;

/**
 * Unit tests for ServicesUpCheck
 *
 */
public class ServicesNamenodeTruncateCheckTest {

  private Clusters m_clusters = EasyMock.createMock(Clusters.class);
  private ServicesNamenodeTruncateCheck m_check = new ServicesNamenodeTruncateCheck();
  private final Map<String, String> m_configMap = new HashMap<String, String>();

  @Before
  public void setup() throws Exception {
    Cluster cluster = EasyMock.createMock(Cluster.class);

    Config config = EasyMock.createMock(Config.class);

    expect(config.getProperties()).andReturn(m_configMap).anyTimes();
    expect(cluster.getService("HDFS")).andReturn(EasyMock.createMock(Service.class));
    expect(cluster.getDesiredConfigByType("hdfs-site")).andReturn(config).anyTimes();
    expect(m_clusters.getCluster((String) anyObject())).andReturn(cluster).anyTimes();

    replay(m_clusters, cluster, config);

    m_check.clustersProvider = new Provider<Clusters>() {
      @Override
      public Clusters get() {
        return m_clusters;
      }
    };
  }


  @Test
  public void testIsApplicable() throws Exception {

    PrereqCheckRequest checkRequest = new PrereqCheckRequest("c1");
    checkRequest.setRepositoryVersion("HDP-2.2.0.0");
    checkRequest.setSourceStackId(new StackId("HDP", "2.2"));
    checkRequest.setTargetStackId(new StackId("HDP", "2.2"));

    Assert.assertTrue(m_check.isApplicable(checkRequest));
  }

  @Test
  public void testPerform() throws Exception {
    PrerequisiteCheck check = new PrerequisiteCheck(null, null);
    m_check.perform(check, new PrereqCheckRequest("c1"));
    assertEquals(PrereqCheckStatus.PASS, check.getStatus());

    m_configMap.put("dfs.allow.truncate", "true");
    check = new PrerequisiteCheck(null, null);
    m_check.perform(check, new PrereqCheckRequest("c1"));
    assertEquals(PrereqCheckStatus.FAIL, check.getStatus());

    m_configMap.put("dfs.allow.truncate", "false");
    check = new PrerequisiteCheck(null, null);
    m_check.perform(check, new PrereqCheckRequest("c1"));
    assertEquals(PrereqCheckStatus.PASS, check.getStatus());
  }
}
