# LinkedData-Logistics
This project is concerned with the verification of Linked Data in the logistics domain.
As ensuring integrity of Linked Data is inherently difficult, we propose a scheme to provide proof in a trustless manner by using *Smart Contracts* on the Ethereum platform.
For a quick overview, see the information provided below or read our research paper [[1](https://github.com/uvdsl/LinkedData-Logistics#sources)], availbale [here](https://link.springer.com/chapter/10.1007%2F978-3-030-33220-4_28)!


For a short showcase, take a look at our demo websites for the Webagent [(BPM 2020)](http://people.aifb.kit.edu/co1683/2020/bpm-demo/), or its predecessor, the CLI [(SEMANTiCS 2019 Demo)](http://people.aifb.kit.edu/co1683/2019/ld-chain/semantics-demo/).
You can also read the corresponding Posters&Demo papers of the Webagent [[3](https://github.com/uvdsl/LinkedData-Logistics#sources)] and the CLI [[2](https://github.com/uvdsl/LinkedData-Logistics#sources)].


Regarding the data environment for the demos, you find all the RDF and blockchain in [data](https://github.com/uvdsl/LinkedData-Logistics/tree/master/data).

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

**Solution.** 
Take a look at the [README of the webagent](https://github.com/uvdsl/LinkedData-Logistics/tree/master/webagent) to find out how to set it up!



## Found a Bug?
Yes! This is a proof of concept, so we would be curious on how to develop it further!

---

## Sources

[1] Braun, CHJ, and Käfer, T: Verifying the Integrity of Hyperlinked Information Using Linked Data and Smart Contracts. In: Proceedings of the 15th International Conference on Semantic Systems (SEMANTiCS) (2019)

[2] Braun, CHJ, and Käfer, T: Verifying the Integrity of Information along a Supply Chain using Linked Data and Smart Contracts. In: Proceedings of Posters and Demos at the 15th International Conference on Semantic Systems (SEMANTiCS) (2019)

[3] Braun, CHJ, and Käfer, T: A Web Agent to Participate in a Scalable Semantic Supply Chain Network on the Blockchain. [Preprint](http://people.aifb.kit.edu/co1683/2020/bpm-demo/paper_bpm20demo_LD_blockchain.pdf).
