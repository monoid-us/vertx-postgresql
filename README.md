vertx-postgresql
================

Asynchronous PostgreSQL driver with support for transactions. 

But, but, there is already a Vert.x module for PostgreSQL. What's the difference?

This driver is used as a library directly in your verticle and supports running transactions against the DB,
i.e. having full control over the transaction along with begin, commit, roll-back etc.

The design goals of this driver are:

* Designed for PostgreSQL
* Memory-usage friendly: little to no copying of result sets in memory, fly-weight pattern in the driver itself
* Support for PostgreSQL advanced type systems (forthcoming)
* Control over number of connections to DB per verticle (defaults to 5 per verticle)

NOTE: As of now the driver is in its infancy. Simple queries work, but there's lack for a number of data conversions
See integration tests for examples.

In initial tests, a single verticle processed 100.000 event bus messages, ran a query for each one using 5 connections in roughly 26 seconds on a Mid 2011 iMac. See EventBusTest.
 
**In this early stage this is a proof of concept, not more.**

**I'd love to hear feedback in the vert.x google group**


How to use
----------

For now, clone the repository and run ./gradlew

Either use the resulting mod or copy the JAR file to your libraries as there is currently no main verticle defined.


Features
----------

* Run single or multiple commands and queries within the context of a transaction (see Transaction.execute)
* Run one or more SELECT statements and receive rows using a simple call-back interface (see ResultListener)
* Run multiple transaction inside a single verticle
* Configurable number of connections per verticle
* Supports simple password login (see limitations/todo)

Limitations/Todo
-----------------

* No support yet for MD5, Kerberos, GSS, SSPI authentication (see Transaction.on(AuthenticationRequest))
* No support for prepared statements
* Lousy error handling
* Even lousier support for data-types and conversion. Only PostgreSQL types defined in class _Types_ with a converter are useable. For now that is Bigint, Integer, Reltime, Smallint, Varchar, Character Varying. The goal is to support all built-in types and have support for custom types as well for validation and conversion




Example code
-------------

(With Java 8, the code below will actually look readable ;)

```java
	Postgres pg;
	public void start() {
	 // in your verticle's start method create an instance of the PostgreSQL driver.
	 // transactions run by this instance below will connect to DB test on localhost using credentials test/test
	 pg = new Postgres(vertx, "test", "test".toCharArray(), "test");
	  
	    // Once a transaction is available, handle(Transaction) will be called. 
	    // Use the trx object to execute commands and run queries
	    // You can use a regular handler or chain calls using the Promise classes in the callback package.
		  p.withTransaction(new Handler<Transaction>() {
			@Override
			public void handle(Transaction trx) {
				trx.execute("BEGIN; " +
					"CREATE TABLE account ( owner character varying (40), amount integer );" +
					"INSERT INTO account (owner, amount) VALUES ('JB', 100), ('LG', 100);").
				done(new TrxCallback() {
					@Override public void handle(Transaction trx) {
						checkAmount(trx);
					}
				});
			}
		});
	}
	
	// Run a query against the DB. In this case only one result is expected.
	// SingleResult makes it reasy to retrieve the result in a single call back
	// Do check out ResultListener, which gives you full control over 
	// how query results are being returned to your verticle!
	private void checkAmount(Transaction trx) {
		trx.query("SELECT amount FROM account WHERE owner = " + owner, new SingleResult<Integer>() {
			@Override public void result(Integer currentAmount, Transaction trx) {
				if (currentAmount < amount) {
					updateAccounts(trx);
				} else {
					System.out.println("Not enough money!");
					trx.execute("ROLLBACK", new Handler<Transaction>() {
						@Override public void handle(Transaction trx) {
							trx.release();
							testComplete();
						}						
					});
				}
			}
		});
	}

	private void updateAccounts(Transaction trx) {
		trx.execute("UPDATE account SET amount = amount - " + amount +  " WHERE owner = " + owner + ";" +
								"UPDATE account SET amount = amount + " + amount + " WHERE owner = " + target + ";").
		done(new TrxCallback() {
			@Override public void handle(Transaction trx) {
			// Note the usage of COMMIT 
				trx.execute("COMMIT; DROP TABLE account", new Handler<Transaction>() {
					@Override public void handle(Transaction trx) {
						trx.release(); // release so other callbacks can use it
						testComplete();
					}
				});
			}
		});
	}
```		
 
