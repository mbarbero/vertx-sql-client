/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleClient;
import io.vertx.oracleclient.OraclePool;
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(VertxUnitRunner.class)
public abstract class OracleGeneratedKeysTestBase extends OracleTestBase {

  private static final String DROP = "DROP TABLE EntityWithIdentity";
  private static final String CREATE = "CREATE TABLE EntityWithIdentity\n" +
    "(\n" +
    "    id       NUMBER(19, 0) GENERATED AS IDENTITY,\n" +
    "    name     VARCHAR2(255 CHAR),\n" +
    "    position NUMBER(10, 0),\n" +
    "    PRIMARY KEY (id)\n" +
    ")";
  private static final String INSERT = "INSERT INTO EntityWithIdentity (name, position) VALUES (?, ?)";

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  protected OraclePool pool;

  @Before
  public void setUp(TestContext ctx) throws Exception {
    pool = OraclePool.pool(vertx, oracle.options(), new PoolOptions());
    pool.withConnection(conn -> {
      return conn.query(DROP).execute()
        .otherwiseEmpty()
        .compose(v -> conn.query(CREATE).execute());
    }, ctx.asyncAssertSuccess());
  }

  @Test
  public void shouldRetrieveRowId(TestContext ctx) {
    doTest(ctx, () -> {
      return null;
    }, generated -> verifyGenerated(generated, "ROWID", byte[].class));
  }

  @Test
  public void shouldRetrieveGeneratedKeyByName(TestContext ctx) {
    doTest(ctx, () -> {
      return new OraclePrepareOptions()
        .setAutoGeneratedKeysIndexes(new JsonArray().add("id"));
    }, generated -> verifyGenerated(generated, "ID", Number.class));
  }

  @Test
  public void shouldRetrieveGeneratedKeyByIndex(TestContext ctx) {
    doTest(ctx, () -> {
      return new OraclePrepareOptions()
        .setAutoGeneratedKeysIndexes(new JsonArray().add(1));
    }, generated -> verifyGenerated(generated, "ID", Number.class));
  }

  private void doTest(TestContext ctx, Supplier<OraclePrepareOptions> supplier, Consumer<Row> checks) {
    OraclePrepareOptions options = supplier.get();
    withSqlClient(client -> {
      return client.preparedQuery(INSERT, options).execute(Tuple.of("bar", 4));
    }, ctx.asyncAssertSuccess(rows -> ctx.verify(v -> {
      checks.accept(rows.property(OracleClient.GENERATED_KEYS));
    })));
    if (options != null) {
      withSqlClient(client -> {
        return client.preparedQuery(INSERT, new PrepareOptions(options.toJson())).execute(Tuple.of("foo", 3));
      }, ctx.asyncAssertSuccess(rows -> ctx.verify(v -> {
        checks.accept(rows.property(OracleClient.GENERATED_KEYS));
      })));
    }
  }

  protected abstract <T> void withSqlClient(Function<SqlClient, Future<T>> function, Handler<AsyncResult<T>> handler);

  private void verifyGenerated(Row generated, String expectedColumnName, Class<?> expectedClass) {
    assertNotNull(generated);
    assertEquals(expectedColumnName, generated.getColumnName(0));
    assertThat(generated.getValue(expectedColumnName), is(instanceOf(expectedClass)));
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close(ctx.asyncAssertSuccess());
  }
}