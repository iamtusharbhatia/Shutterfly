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

### Performance characteristic

1. Keeping Performance in mind I have tried to use HashMap for the `in-memory Data Structure` instead of lists, wherever frequent searching is required, so that we can reduce the lookup time to **O(1)**.
2. I have used `Priority Queue` to fetch the top **X Customer Lifetime Values** which will give us the least completxity.

### Assumptions

1. Average customer lifespan for every customer is 10 years.
2. If an order has been placed then it is assumed that, there is also a site_visit entry in the input data corresponding to that order.
3. While updating customer we are just updating last_name, adr_city, adr_state and not `event_time` because if we update it, we will lose the customer creation date which is necessary for LTV calculation.
4. While updating orders we are just updating event_time, total_amount and not customer_id  because we need to preserve customer id corresponding to a particular order.

### External Libraries Used

Gson --> 'gson-2.2.3.jar'
http://www.java2s.com/Code/JarDownload/gson/gson-2.2.3.jar.zip
