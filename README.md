# Shutterfly
Shutterfly Customer Lifetime Value Code-Challenge

### Critical Design Decision

* For any event if any of "required data" fields has any missing or null value then that event's data won't be ingested into the Data Structure.
* I have created an additional Data Structure to take care of the events which occur in an unordered way, like if a new order comes before the create event for that customer.

### Assumptions

1) Average customer lifespan for every customer is 10 years.
2) I am assuming while updating customer we are just updating last_name, adr_city, adr_state and not event_time because if we update it we'll lose the customer creation date which is necessary for LTV calculation.
3) I am assuming while updating orders we are just updating event_time, total_amount and not customer_id  as it does not make sense updating a customer id corresponding to an order.

### External Libraries Used

Please download 'gson-2.2.3.jar' and add as an external jar to the project.
http://www.java2s.com/Code/JarDownload/gson/gson-2.2.3.jar.zip
