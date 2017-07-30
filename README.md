# Shutterfly
Shutterfly Customer Lifetime Value Code-Challenge

### Critical Design Decision

* For any event if the **`required data`** is missing or has null value, then that event's data would not be ingested into the `in-memory Data Structure`.
* An `additional Data Structure` has been created to handle the events which occur in an `unordered way`, like:
    * A new order comes before the create event for that particular customer
    * An Updation to an order comes before creation of that order
* The events are processed in such a way that all the ordered events will be ingested in the `in-memory Data Structure`, and all of the unordered events are inserted into the `additional Data Structure`. 
* Once all the ordered events have been processed then the additional Data Structure will be processed to inserts the unordered event in the `in-memory Data Structure`.

### Assumptions

1) Average customer lifespan for every customer is 10 years.
2) I am assuming while updating customer we are just updating last_name, adr_city, adr_state and not event_time because if we update it we'll lose the customer creation date which is necessary for LTV calculation.
3) I am assuming while updating orders we are just updating event_time, total_amount and not customer_id  as it does not make sense updating a customer id corresponding to an order.

### External Libraries Used

Please download 'gson-2.2.3.jar' and add as an external jar to the project.
http://www.java2s.com/Code/JarDownload/gson/gson-2.2.3.jar.zip
