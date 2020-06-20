# CLI - Linked Data Chain Client
In this demo, we present our CLI. The corresponding Posters&Demo paper is available [here](http://ceur-ws.org/Vol-2451/paper-07.pdf).
Our implementation consists of the following parts:

1. We use Linked Data, i.e. RDF accessible using HTTP to store data off-chain in a decentralised fashion. <br>
   We build a trail of ownership of an item, its so-called ''Linked Pedigree'', in the form of RDF graphs. <br>
   The RDF graphs are described using terms from the OntoPedigree ontology [[2](https://github.com/uvdsl/LinkedData-Logistics\cli#sources)].
2. We implement a link-traversal based querying approach for verifying data on a Linked Pedigree off-chain.
3. We hash the RDF graph using the approach of [[1](https://github.com/uvdsl/LinkedData-Logistics\cli#sources)] to connect the off-chained data with the distributed ledger.
4. We implement a Smart Contract to store RDF graph hashes in a Distributed Ledger based on Ethereum. <br>
   The stored hashes are then used to verify their corresponding Linked Data.

The implementation of 2 and 4 as well as the unique combination of the single parts are the contributions of this demonstration.

<br>

For a short showcase, you can follow the instructions provided in [demo_instructions.txt](https://github.com/uvdsl/LinkedData-Logistics/blob/master/cli/demo_instructions.txt) or take a look at our [demo website](http://people.aifb.kit.edu/co1683/2019/ld-chain/semantics-demo/).

---

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
geth --port 3000 --networkid 7331 --nodiscover --datadir=./LinkedDataChainClient/ethereum --rpc --rpcport 8543 --rpcapi "admin,eth,net,web3,personal,miner"
```

## Build and run the LinkedDataChainClient
```bash
cd ./LinkedDataChainClient
mvn package
java -Xmx1024M -jar ./target/LinkedDataChainClient-jar-with-dependencies.jar
```

---

## Sources

[1] Hogan, A: Skolemising Blank Nodes while Preserving Isomorphism. In: Proceedingsof the 24th International Conference on World Wide Web (WWW) (2015)

[2] Solanki, M and Brewster, C: Consuming Linked data in Supply Chains: Enablingdata visibility via Linked Pedigrees. In: Proceedings of the 4th International Work-shop on Consuming Linked (COLD) at the 12th International Semantic Web Con-ference (ISWC) (2013)
