# LinkedData-Logistics
This project is concerned with the verification of Linked Data in the logistics domain.
As ensuring integrity of Linked Data is inherently difficult, we propose a scheme to provide proof in a trustless manner by using *Smart Contracts* on the Ethereum platform.
For a quick overview, see the information provided [below](https://github.com/uvdsl/LinkedData-Logistics#overview-problem--solution-approach) or read [our paper](tbd)!

<p>

For a short showcase, you can follow the instructions provided in [demo_instructions.txt](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/demo_instructions.txt) or take a look at our [demo website](http://people.aifb.kit.edu/co1683/2019/ld-chain/semantics-demo/).

## Prerequisite for LinkedDataChainClient
a) Have [NxParser](https://github.com/nxparser/nxparser) version 3.0.0-SNAPSHOT installed. If not, quickly:
```bash
cd /tmp/
git clone https://github.com/nxparser/nxparser
cd nxparser
mvn clean install -Dmaven.test.skip=true
```

b) Have [Geth](https://geth.ethereum.org/downloads/) installed. This is a Go implementation of an Ethereum-Node. Run it:
```bash
geth --port 3000 --networkid 7331 --nodiscover --datadir=./LinkedDataChainClient/ethereum --rpc --rpcport 8543 --rpcapi "admin,eth,net,web3,personal,miner" --ipcdisable
```

## Build and run the LinkedDataChainClient
```bash
cd ./LinkedDataChainClient
mvn package
java -Xmx1024M -jar ./target/LinkedDataChainClient-jar-with-dependencies.jar
```

<p>

**Reminder:**
Instructions for a short demonstration can be found in [demo_instructions.txt](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/demo_instructions.txt). <br>
A website presenting a walk-through of the ''LD-chain client'', this CLI client implementation, and further explanation can be found [here](http://people.aifb.kit.edu/co1683/2019/ld-chain/semantics-demo/).
<br>

---

## Overview: Problem & Solution Approach
**Problem.**
Today's supply chain networks face the challenge of delivering goods and services not only efficiently but also transparently to the customer.
Transparently provided information is important, e.g. in the food sector, where society demands more transparency regarding details on products and their transportation.

Transparency and data immutability are focus of Distributed Ledger Technologies, which they gained quite the attention for in recent years.
However, the ongrowing storage cost of data on the ledger calls for off-chaining.
Data is stored outside of the legder and only hashes are stored on the ledger as a reference.

For off-chaining, a uniform access mechanism would be desired.
Linked Data is a light-weight standard-based way to publish data in a decentralised fashion, where access control can be easily implemented.

Hence, we ask: Can we combine the verification capabilities of the distributed ledger with Linked Data management?

<br>

**Solution.** In this demo, we present our implementation and an use-case from the supply chain domain.
The implementation of 2 and 4 as well as the unique combination of the single parts are the contributions of this demonstration.
Our implementation consists of the following parts:

1. We use Linked Data, i.e. RDF accessible using HTTP to store data off-chain in a decentralised fashion. <br>
   We build a trail of ownership of an item, its so-called ''Linked Pedigree'', in the form of RDF graphs. <br>
   The RDF graphs are described using terms from the OntoPedigree ontology [[2](https://github.com/uvdsl/LinkedData-Logistics#sources)].
2. We implement a link-traversal based querying approach for verifying data on a Linked Pedigree off-chain.
3. We hash the RDF graph using the approach of [[1](https://github.com/uvdsl/LinkedData-Logistics#sources)] to connect the off-chained data with the distributed ledger.
4. We implement a Smart Contract to store RDF graph hashes in a Distributed Ledger based on Ethereum. <br>
   The stored hashes are then used to verify their corresponding Linked Data.

<br>

---

## Found a Bug?
Yes! If you play around a little, you will find, that the implementation of the Smart Contract may not satisfy your needs.
This code should not be deployed in a real business environment without review, rather, it can be used as a template to develop a robust and to your use case applicable Smart Contract.

## Sources

[1] Hogan, A: Skolemising Blank Nodes while Preserving Isomorphism. In: Proceedingsof the 24th International Conference on World Wide Web (WWW) (2015)

[2] Solanki, M and Brewster, C: Consuming Linked data in Supply Chains: Enablingdata visibility via Linked Pedigrees. In: Proceedings of the 4th International Work-shop on Consuming Linked (COLD) at the 12th International Semantic Web Con-ference (ISWC) (2013)
