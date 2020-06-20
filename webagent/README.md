# Webagent - Linked Data Chain Agent (Version 1)
In this demo, we present our webagent. The corresponding Posters&Demo paper is available [here](tbd).
Our implementation builds on the CLI [[1](https://github.com/uvdsl/LinkedData-Logistics/webagent#sources)]. It thus consists of the following parts:

1. We use Linked Data, i.e. RDF accessible using HTTP to store data off-chain in a decentralised fashion. <br>
   We build a trail of ownership of an item, its so-called ''Linked Pedigree'', in the form of RDF graphs. <br>
   The RDF graphs are described using terms from the OntoPedigree ontology [[3](https://github.com/uvdsl/LinkedData-Logistics/webagent#sources)].
2. We implement a link-traversal based querying approach for verifying data on a Linked Pedigree off-chain.
3. We hash the RDF graph using the approach of [[2](https://github.com/uvdsl/LinkedData-Logistics/webagent#sources)] to connect the off-chained data with the distributed ledger.
4. We implement a Smart Contract to store RDF graph hashes in a Distributed Ledger based on Ethereum. <br>
   The stored hashes are then used to verify their corresponding Linked Data.

5. We base the agent's architecture on the practices around the decentralised social network [SoLiD](https://solid.mit.edu), which is based on RESTful communication, data modellingin RDF, and Linked Data Notifications as ontology and protocol to describeand interact with notifications. 
6. We tie the social network to the interlinked history of a product using the standardised [Activity Streams vocabulary](https://www.w3.org/TR/activitystreams-vocabulary/).
   The agent is centred around the URI that identifiesa supply chain network participant. 
   
The agent thus allows for participation in the supply chain network, manages the notifications, and verifies the data usingthe blockchain.
The implementaion of 1-4 are basically a rewrite of the Java code in Javascript to put a new shiny UI on top of it.
The implementation of 5 and 6 as well as the unique combination of the single parts are the contributions of this demonstration.

For a short showcase, take a look at our [demo website](http://people.aifb.kit.edu/co1683/2020/bpm-demo/).


## Prerequisite for ldca_v1
```
cd ./ldva_v1
npm install
```
In addition, you need a Ethereum Blockchain with an instance of the HashStore Smart Contract as well as user profiles described using RDF. Have a look at the [data folder](https://github.com/uvdsl/LinkedData-Logistics/data)!
You need an instance of an Linked Data hasher running, e.g. [LDH](https://github.com/uvdsl/ldh).
For the inbox and outbox, you can use a basic Linked Data container like this one, [LDBBC](https://github.com/kaefer3000/ldbbc).

## Build and run with Docker
```
docker build -t ldca_v1:latest ./ldca_v1
docker run -d --name LDCA_v1 ldca_v1:latest
```


---

## Sources

[1] Braun, CHJ, and Käfer, T: Verifying the Integrity of Information along a SupplyChain using Linked Data and Smart Contracts. In: Proceedings of Posters andDemos at the 15th International Conference on Semantic Systems (SEMANTiCS)(2019)

[2] Hogan, A: Skolemising Blank Nodes while Preserving Isomorphism. In: Proceedingsof the 24th International Conference on World Wide Web (WWW) (2015)

[3] Solanki, M and Brewster, C: Consuming Linked data in Supply Chains: Enablingdata visibility via Linked Pedigrees. In: Proceedings of the 4th International Work-shop on Consuming Linked (COLD) at the 12th International Semantic Web Con-ference (ISWC) (2013)
