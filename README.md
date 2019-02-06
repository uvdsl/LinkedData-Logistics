# LinkedData-Logistics
This project is concerned with the usage of Linked Data in the logistics domain. As ensuring integrity of Linked Data is inherently difficult, we propose a scheme to provide proof in a trustless manner by using *Smart Contracts* on the Ethereum platform. For a quick overview, see the information provided [below](https://github.com/uvdsl/LinkedData-Logistics#overview-problem--solution-approach) or read [our paper](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/literature/Braun%2C%20Merker%2C%20Leuthe%20(2019)%2C%20Data%20sharing%20in%20the%20logistics%20domain.pdf)!<p>
        
However, the implementation is more of a [demonstration tool](https://github.com/uvdsl/LinkedData-Logistics#demonstration-tool), rather than a at production stage deployable application. For a short showcase, you can follow the instructions provided in [demo_instructions.txt](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/demo_instructions.txt).

## Prerequisite for LinkedDataLogisticsClient
a) Have [Geth](https://geth.ethereum.org/downloads/) installed. This is a Go implementation of an Ethereum-Node.<br>
b) Have [NxParser](https://github.com/nxparser/nxparser) version 3.0.0-SNAPSHOT installed. If not, quickly:
```bash
cd /tmp/
git clone https://github.com/nxparser/nxparser
cd nxparser
mvn clean install -Dmaven.test.skip=true
```

## Build and run the LinkedDataLogisticsClient
```bash
mvn package
java -Xmx1024M -jar ./target/LinkedDataLogisticsClient-jar-with-dependencies.jar
```

**Reminder:** Instructions for a short demonstration can be found in [demo_instructions.txt](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/demo_instructions.txt).

<br>

---

## Overview: Problem & Solution Approach
**Problems.** Data sharing is the basis of a well working supply chain. Companies rely on information they receive from their business partners not only to operate efficiently in their daily business, but also when taking important business decisions. In addition, not only businesses but also society, clearly demanding more transparency regarding details on consumer products and their transportation, require a way of receiving and verifying information in a standardised manner.<p>
        
**Solution.** To solve this problem, we propose the use and implementation of a *Linked Pedigree*, a form of a decentralized dataset representing the biography of a product. This *Linked Pedigree* is hashed and its hashes then stored via a *Smart Contract* on a blockchain, to enable for a verification process of that biography data. 
The *Linked Pedigree* is structured according to the [LinkedPedigree-Ontology](http://www.student.kit.edu/~uvdsl/ontologies/linkedPedigree.ttl) developed by us.<p>
        
## Linked Pedigree
In current supply chains, the standard is to only receive information one (business partner) up
and one down. Usually information from further business partners is not propagated. To change this, we use a *Linked Pedigree*, as initially proposed by [1]. This event-based decentralised database of the product's biography enables a user to trace back each step in the product's biography to gain access to the complete product history. Each step in the biography is documented by its own *Linked Pedigree* part. <p>

Starting with the producer of the product, the first part of the *Linked Pedigree* is a "[birth certificate](http://www.student.kit.edu/~uvdsl/data/birthCert.ttl)" of the product. It contains information about the product and its production. While the product moves through the supply chain, transfers from suppliers to reciepents of the product within the supply chain generate data. This data is documented in "[transfer documents](http://www.student.kit.edu/~uvdsl/data/linkedTransfer1.ttl)". Each transfer document refers to the transfer document of the previous product transfer. The first transfer of the product refrences its birth certificate, thereby associating the *Linked Pedigree* "chain" with that exact product. <p>
        
When retrieving a *Linked Pedigree*, the *Linked Pedigree* "chain" is traversed to recursively retrieve the single parts of the *Linked Pedigree* until the birth certificate is reached.<p>
        
**Figure 1: Structure of a _Linked Pedigree_**
![LinkedPedigree](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/images/linkedPedigree.png)

## Smart Contract
The *Smart Contract* represents a notarial authority providing proof for (multiple) *Linked Pedigrees’* Linked Data via the data’s syntactic hashes.<p>
        
In some way, the functional structure of the *Smart Contract* is similar to that of a distributed hash table. Hashes of Linked Data can be passed to the *Smart Contract* to be stored. Hashes cannot be altered or removed, once they are stored. After retrieving a hash from the *Smart Contract*, it can be compared to the hash directly generated from the Linked Data to verify that the data has not been changed since the hash had been passed to the *Smart Contract*.<p>
        
To keep track of which hashes belong to which physical good’s *Linked Pedigree*, the underlying physical good must be registered with the *Smart Contract*. Only after the hash of the good’s birth certificate has been passed to the *Smart Contract*, any other hashes regarding that physical good can be stored.<p>
        
When storing a hash, the *Smart Contract* assigns an index to keep the hashes in a sequential order. The index of the hash matches the position of the corresponding linked pedigree part in its *Linked Pedigree* “chain”. This way, it is not only possible to compare the hashes, but also to check whether the hash is mapped to the correct linked pedigree part.<p>
        
Retrieving hashes from the *Smart Contract* is unrestricted, so that anyone can verify the integrity of the corresponding Linked Data. However, storing new hashes via the *Smart Contract* is only allowed for the current owner of the physical good. The *Smart Contract* keeps track of the current owner of every physical good known to the Smart Contract. The current owner is only allowed to add one hash and is then forced to transfer the digital ownership of the good via the Smart Contract to the next owner.<p>
        
For the use case at hand, **Figure 2** sums up the interaction between *Linked Pedigree* and *Smart Contract* as well as the relation to the physical good transfer.<p>
        
**Figure 2: UML Use Case Diagram**
![UseCaseDiagram](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/images/Use%20case.png)

## Transfer Workflow
By integrating the previously introduced ideas into the use case at hand, a strict transfer protocol has been developed, as depicted by **Figure 2**. When a transfer of a physical good is initialized, several actions need to be taken successfully, before the receiving party may accept the physical good.<p>
        
First, the supplier generates the Linked Data associated with the transfer, which is the next *Linked Pedigree* part, and makes it accessible to the receiving party. The hash of that *Linked Pedigree* part is passed to the *Smart Contract* to be stored. Immediately,
the digital ownership of the physical good is transferred to the receiving party via the *Smart Contract*.<p>
        
Then, the recipient verifies that the information provided by the supplier is true in a Tarskian sense, meaning that the transfer is in fact carried out as stated in the generated *Linked Pedigree* part. Furthermore, the recipient traverses the single *Linked pedigree* parts until the birth certificate of the physical good is reached. The *Linked Pedigree’s* integrity is checked by comparing hashes directly calculated from each *Linked Pedigree* part with the hash for that part provided by the *Smart Contract*. Only if the integrity of the *Linked Pedigree* is intact and the generated transfer data is true, the physical good is accepted by the recipient and the transfer is completed. In case of inconsistencies during the transfer process, the physical good may be classified as defective. The receiving party can therefore reject the physical good, return the digital ownership to the supplier and cancel the transfer.

## Demonstration Tool 
To showcase the approach proposed, a minimal viable product (MVP) application has been developed. The implemented functionalities include retrieval of *Linked Pedigrees*, syntactic hashing of Linked Data and verifying the hashes of a *Linked Pedigree* against a *Smart Contract* on an Ethereum network. The *Smart Contract* can handle the hashes multiple *Linked Pedigrees*.<p>
        
For the use case at hand, the **Figure 3** shows the implemented procedure for the proposed transfer protocol.<p>
        
**Figure 3: UML Sequence Diagram**
![SequenceDiagram](https://github.com/uvdsl/LinkedData-Logistics/blob/master/resources/images/Sequence%20Diagram.png)



## Found a Bug?
Yes! If you play around a little, you will find, that the implementation of the Smart Contract is not really as precise as declared in the paper. To ease the pain of strict business rules and facilitate the demonstration, we decided to keep the Smart Contract that way, knowing very well about its flaws. This code should not be deployed in a real business environment, rather, it can be used as a template to develop a robust and to your use case applicable Smart Contract.

## Sources
[1] Monika Solanki, Christopher Brewster (2013): Consuming Linked data in Supply Chains: Enabling data visibility via Linked Pedigrees 
