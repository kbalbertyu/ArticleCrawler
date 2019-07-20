var ioc = {
  dao : {
    type : "org.nutz.dao.impl.NutDao",
    args : [{refer:"dataSource"}]
  },
  dataSource : {
    type : "org.nutz.dao.impl.SimpleDataSource",
    fields : {
      jdbcUrl : 'jdbc:sqlite:db/base.db'
    }
  }
}