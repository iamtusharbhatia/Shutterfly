# Shutterfly
> Shutterfly Customer Lifetime Value Code-Challenge

### Critical Design Decision

1. For any event if the `required data` is missing or has null value, then that event's data would not be ingested into the `in-memory Data Structure`.
2. An `additional Data Structure` has been created to handle the events which occur in an `unordered way`, like:
    * A new order comes before the create event for that particular customer
    * An Updation to an order comes before creation of that order
3. The events are processed in such a way that all the ordered events will be ingested in the `in-memory Data Structure`, and all of the unordered events are inserted into the `additional Data Structure`. 
4. Once all the ordered events have been processed then the additional Data Structure will be processed to inserts the unordered event in the `in-memory Data Structure`.
5. Since `revenue / visit` and `visits / week` are important metrics to the business so, there are two functions **revenuePerVisits** & **visitsPerWeek** which help in achieving these functionalities.

### Assumptions

1. Average customer lifespan for every customer is 10 years.
2. While updating customer we are just updating last_name, adr_city, adr_state and not event_time because if we update it we'll lose the customer creation date which is necessary for LTV calculation.
3. While updating orders we are just updating event_time, total_amount and not customer_id  as it does not make sense updating a customer id corresponding to an order.

### External Libraries Used

Gson --> 'gson-2.2.3.jar'
http://www.java2s.com/Code/JarDownload/gson/gson-2.2.3.jar.zip
