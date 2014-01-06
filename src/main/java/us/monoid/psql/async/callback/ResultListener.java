package us.monoid.psql.async.callback;


/** Implement this interface (or use one of the existing implementations) to read the results of your query.
 * Note that this is a combination of three simpler interfaces to make it easy to use lambda expressions when getting results from the database.
 *  */
public interface ResultListener extends ResultStart, ResultRow, ResultEnd {

}
