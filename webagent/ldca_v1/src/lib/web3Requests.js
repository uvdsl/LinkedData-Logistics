"use strict";

var Web3 = require("web3");
const UNLOCK_DURATION = 600;
var web3;

async function connect(ledgerCon) {
  console.log("### WEB3 \t| Connecting ...\n" + ledgerCon);
  const provider = new Web3.providers.HttpProvider(ledgerCon);
  const w3 = new Web3(provider);
  return w3.eth.net
    .isListening()
    .then(() => {
      console.log("### WEB3 \t| Connected to the node!\n");
      if (web3 === undefined) web3 = w3;
      return true;
    })
    .catch(() => {
      console.log("### WEB3 \t| Could not reach the node.");
      return undefined;
    });
}

// contact eth node to get validation info
async function getValidationInfo(uri, contractAddress, ledgerCon) {
  if (web3 === undefined) {
    await connect(ledgerCon);
  }
  const contract = new web3.eth.Contract(CONTRACT_ABI, contractAddress);
  return Promise.all([
    contract.methods.getHash(uri).call(),
    contract.methods.getPreviousPedigreeURI(uri).call(),
    contract.methods.getOwner(uri).call(),
    contract.methods.getApproval(uri).call()
  ]).then(info => {
    return {
      uri: uri,
      hash: info[0],
      prevPart: info[1],
      owner: info[2],
      approval: info[3]
    };
  });
}

// contact eth node to approve a hash
async function approveHash(uri, contractAddress, wallet, pass, ledgerCon) {
  if (web3 === undefined) {
    await connect(ledgerCon, contractAddress);
  }
  const contract = new web3.eth.Contract(CONTRACT_ABI, contractAddress);
  return web3.eth.personal
    .unlockAccount(wallet, pass, UNLOCK_DURATION)
    .then(() => console.log("Account unlocked! (" + wallet + ")"))
    .then(() => contract.methods.approve(uri).encodeABI())
    .then(txData => execTx(wallet, contractAddress, txData))
    .then(() => web3.eth.personal.lockAccount(wallet))
    .then(() => console.log("Account locked! (" + wallet + ")"));
}

// contact eth node to store a hash
async function storeHash(
  uri,
  prevURI,
  hash,
  nextOwnerWallet,
  contractAddress,
  wallet,
  pass,
  ledgerCon
) {
  if (web3 === undefined) {
    await connect(ledgerCon, contractAddress);
  }
  const contract = new web3.eth.Contract(CONTRACT_ABI, contractAddress);
  return web3.eth.personal
    .unlockAccount(wallet, pass, UNLOCK_DURATION)
    .then(() => console.log("Account unlocked! (" + wallet + ")"))
    .then(() => {
      let txData;
      if (prevURI == null || prevURI === undefined) {
        // init product
        txData = contract.methods
          .initProduct(
            uri, // pedigree URI
            web3.utils.hexToBytes(hash), // hash in bytes16
            nextOwnerWallet
          ) // next owner address
          .encodeABI();
      } else {
        // append uri
        txData = contract.methods
          .storeAndTransfer(
            prevURI, // prev Pedigree URI
            uri, // pedigree URI
            web3.utils.hexToBytes(hash), // hash in bytes16
            nextOwnerWallet
          ) // next owner address
          .encodeABI();
      }
      return txData;
    })
    .then(txData => execTx(wallet, contractAddress, txData))
    .then(() => web3.eth.personal.lockAccount(wallet))
    .then(() => console.log("Account locked! (" + wallet + ")"));
}

// execute Transaction
async function execTx(wallet, contractAddress, txData) {
  web3.eth.defaultAccount = wallet;
  const nonceval = await web3.eth.getTransactionCount(wallet);
  const price = await web3.eth.getGasPrice();
  const tx /*: TransactionConfig*/ = {
    nonce: nonceval,
    from: web3.eth.defaultAccount,
    to: contractAddress,
    data: txData, // approve, init or append
    gas: 2000000,
    gasPrice: price
  };
  return web3.eth.signTransaction(tx, wallet).then(st => {
    return web3.eth.sendTransaction(st.tx);
  });
}

const CONTRACT_ABI = [
  {
    constant: false,
    inputs: [
      {
        name: "previousPedigreeURI",
        type: "string"
      }
    ],
    name: "approve",
    outputs: [],
    payable: false,
    stateMutability: "nonpayable",
    type: "function"
  },
  {
    constant: false,
    inputs: [
      {
        name: "pedigreeURI",
        type: "string"
      },
      {
        name: "graphHash",
        type: "bytes16"
      },
      {
        name: "newOwner",
        type: "address"
      }
    ],
    name: "initProduct",
    outputs: [],
    payable: false,
    stateMutability: "nonpayable",
    type: "function"
  },
  {
    constant: false,
    inputs: [
      {
        name: "previousPedigreeURI",
        type: "string"
      },
      {
        name: "pedigreeURI",
        type: "string"
      },
      {
        name: "graphHash",
        type: "bytes16"
      },
      {
        name: "newOwner",
        type: "address"
      }
    ],
    name: "storeAndTransfer",
    outputs: [],
    payable: false,
    stateMutability: "nonpayable",
    type: "function"
  },
  {
    constant: true,
    inputs: [
      {
        name: "pedigreeURI",
        type: "string"
      }
    ],
    name: "getApproval",
    outputs: [
      {
        name: "approval",
        type: "bool"
      }
    ],
    payable: false,
    stateMutability: "view",
    type: "function"
  },
  {
    constant: true,
    inputs: [
      {
        name: "pedigreeURI",
        type: "string"
      }
    ],
    name: "getHash",
    outputs: [
      {
        name: "hash",
        type: "bytes16"
      }
    ],
    payable: false,
    stateMutability: "view",
    type: "function"
  },
  {
    constant: true,
    inputs: [
      {
        name: "pedigreeURI",
        type: "string"
      }
    ],
    name: "getOwner",
    outputs: [
      {
        name: "ownerAddress",
        type: "address"
      }
    ],
    payable: false,
    stateMutability: "view",
    type: "function"
  },
  {
    constant: true,
    inputs: [
      {
        name: "pedigreeURI",
        type: "string"
      }
    ],
    name: "getPreviousPedigreeURI",
    outputs: [
      {
        name: "previousPedigreeURI",
        type: "string"
      }
    ],
    payable: false,
    stateMutability: "view",
    type: "function"
  }
];

module.exports = { getValidationInfo, approveHash, storeHash };
