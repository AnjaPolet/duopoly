UNDER CONSTRUCTION

create a stateless webapp that
 * displays you a page that allows to to generate a graph with N nodes (server side), for every node there should be
 ** 100% chance it's connected to one other node
 ** 50% chance that it's connected to a second node
 ** 25% chance that it's connected to a third node
 ** and so on, stop at the "first miss"
 * the graph generation should be a "REST" call, that returns a json representing the graph
 * the web page must render the graph, all nodes and their interconnects in grey (tip: render nodes on a circle for easiest representation)
 * note the model both on the java side as the JS side needs to be able to represent directed and undirected, weighted and unweighted graphs
 * create a set of rest calls that implements
 ** is graph fully interconnected
 ** shortest path between two points
 ** ...
 * each of the calls should recieve the graph model, plus assignment
 * a websocket should be opened that receives updates of the algorithm as it is executed
 * the updates must be shown on the page as they come in
 
 
 
 