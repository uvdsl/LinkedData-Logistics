pragma solidity ^0.5.3;
// Proof of Existence contract
contract LDHashNotary {


    mapping(string =>  mapping (uint => string)) private productMap; // { product : { index : hash } }
    mapping(string => uint) indexMap; // { product : number of storedHashes }
    mapping(string => address) ownerMap; // {product : currentOwner }
    
    modifier currentOwnerOnly(string memory product) {
        require(msg.sender == ownerMap[product], "Sender not authorized.");
        _;
    }
    
    modifier noOverride(string memory product){
        require(indexMap[product]==0, "Product override not allowed.");
        _;
    }
    
    function addNewProduct(string memory product, string memory birthCertHash, address newOwner) public noOverride(product) {
        ownerMap[product] = msg.sender;
        notarizeAndTransfer(product, birthCertHash, newOwner);
      
    }
    
    // lookUp current index as count of hashes in mapping
    function getCount(string memory product) public view returns(uint){
        return indexMap[product];
    }
    // lookUp current owner
    function getOwner(string memory product) public view returns(address) {
        return  ownerMap[product];
    }
    // check if the hash of linked pedigree part {id} matches
    function lookUpHashById(string memory product, uint id) public view returns (string memory result) {
        return  productMap[product][id];
    }
    // notarize hash and transfer ownership
    function notarizeAndTransfer(string memory product, string memory hashDL, address newOwner) public currentOwnerOnly(product) {
        productMap[product][indexMap[product]++] = hashDL;
        ownerMap[product] = newOwner;
    }

    //
    //
    // dev functions
    //
    //
    function notarize(string memory product, string memory hashDL) public currentOwnerOnly(product) {
        productMap[product][indexMap[product]++] = hashDL;
    }
    function transfer(string memory product, address newOwner) public {
        ownerMap[product] = newOwner;
    }
}
