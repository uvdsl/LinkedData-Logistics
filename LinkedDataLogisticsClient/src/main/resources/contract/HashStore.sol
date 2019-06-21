pragma solidity ^0.5.1;

// Proof of Existence contract
contract HashStore {

    // ##################
    // ### properties ###
    // ##################
    /**
     * storing rights
     */
    mapping(string => address) private ownerMap;  // { previousPedigreeURI : currentOwner }
    /**
     * for pedigreeURI retrieval
     */
    mapping(string => string) private previousPedigreeMap; // { pedigreeURI : previousPedigreeURI }

    /**
     * for hash retrieval
     */
    mapping (string => bytes16) private hashMap; // { pedigreeURI : hash }
     /**
     * for pedigree approval
     */
    mapping (string => bool) private approvalMap; // { pedigreeURI : boolean }

    // ##################
    // #### modifier ####
    // ##################
    modifier currentOwnerOnly(string memory pedigreeURI) {
        require(msg.sender == ownerMap[pedigreeURI], "Sender not authorised.");
        _;
    }
    modifier noHashOverride(string memory pedigreeURI){
        require(hashMap[pedigreeURI] == 0, "Hash override not allowed.");
        _;
    }
    modifier approvedOnly(string memory previousPedigreeMapping) {
        require(approvalMap[previousPedigreeMapping] == true, "Previous Pedigree Part has not been approved.");
        _;
    }

    // ################
    // #### memory ####
    // ################
    /**
      * register a new product,
      * initialise previousPedigreeMapping,
      * initialise owner map
      */
    function initProduct(string memory pedigreeURI, bytes16 graphHash, address newOwner) public noHashOverride(pedigreeURI) {
        // update previousPedigreeMap
        previousPedigreeMap[pedigreeURI] = "initial";
        // update hashMap
        hashMap[pedigreeURI] = graphHash;
        // transfer ownership
        ownerMap[pedigreeURI] = newOwner;
    }
    /**
     * extend productMap,
     * append pedgreeURI,
     * update latestPedigreeURI,
     * store hash,
     * and transfer ownership
     */
    function storeAndTransfer(string memory previousPedigreeURI, string memory pedigreeURI, bytes16 graphHash, address newOwner)
      public currentOwnerOnly(previousPedigreeURI) approvedOnly(previousPedigreeURI) noHashOverride(pedigreeURI) {
        // update previousPedigreeMap
        previousPedigreeMap[pedigreeURI] = previousPedigreeURI;
        // update hashMap
        hashMap[pedigreeURI] = graphHash;
        // transfer ownership
        ownerMap[pedigreeURI] = newOwner;
    }
    /**
     * Approve a PedigreePart
     */
    function approve(string memory previousPedigreeURI) public currentOwnerOnly(previousPedigreeURI) {
        approvalMap[previousPedigreeURI] = true;
    }

    // ################
    // #### getter ####
    // ################
    /**
     * lookUp currentOwner by currentPedigreeURI
     */
    function getOwner(string memory pedigreeURI) public view returns(address ownerAddress) {
        return  ownerMap[pedigreeURI];
    }
     /**
     * lookUp previousPedigreeURI by pedgreeURI
     */
    function getPreviousPedigreeURI(string memory pedigreeURI) public view returns(string memory previousPedigreeURI) {
        return previousPedigreeMap[pedigreeURI];
    }
    /**
     * lookUp hash by pedigreeURI
     */
    function getHash(string memory pedigreeURI) public view returns (bytes16 hash) {
        return  hashMap[pedigreeURI];
    }
    /**
     * lookUp approval by pedigreeURI
     */
     function getApproval(string memory pedigreeURI) public view returns (bool approval) {
         return approvalMap[pedigreeURI];
     }
}