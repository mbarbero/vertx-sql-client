package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link UserDataObject}.
 * NOTE: This class has been automatically generated from the {@link UserDataObject} original class using Vert.x codegen.
 */
public class UserDataObjectRowMapper implements java.util.function.Function<io.vertx.sqlclient.Row, UserDataObject> {

  public static final java.util.function.Function<io.vertx.sqlclient.Row, UserDataObject> INSTANCE = new UserDataObjectRowMapper();

  public static final java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<UserDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE, java.util.stream.Collectors.toList());

  public UserDataObject apply(io.vertx.sqlclient.Row row) {
    UserDataObject obj = new UserDataObject();
    Object val;
    int idx;
    if ((idx = row.getColumnIndex("first_name")) != -1 && (val = row.getString(idx)) != null) {
      obj.setFirstName((java.lang.String)val);
    }
    if ((idx = row.getColumnIndex("id")) != -1 && (val = row.getLong(idx)) != null) {
      obj.setId((long)val);
    }
    if ((idx = row.getColumnIndex("last_name")) != -1 && (val = row.getString(idx)) != null) {
      obj.setLastName((java.lang.String)val);
    }
    return obj;
  }
}
