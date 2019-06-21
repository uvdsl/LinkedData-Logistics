package edu.kit.eth;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.ens.EnsResolutionException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

/**
 * This Class is implements a client for interaction with smart contracts on the
 * ethereum blockchain.
 * 
 * @author Christoph
 *
 */
public class Web3jClient {

	private Web3j web3j;
	private Credentials credentials;
	private StaticGasProvider gasProvider; // new StaticGasProvider(gasPrice, gasLimit)
	private static final String DEFAULT_CONNECTION = "http://127.0.0.1:8543"; // as default

	// private TransactionManager
	// Map<Product,Contract>

	/**
	 * Default Constructor, connecting to {@link http://127.0.0.1:8543} with default
	 * gas values.
	 * 
	 * @param walletPass   password to unlock wallet
	 * @param walletSource path to wallet
	 * @throws CipherException 
	 * @throws IOException 
	 */
	public Web3jClient(String walletPass, String walletSource) throws IOException, CipherException {
		this.web3j = Web3j.build(new HttpService(DEFAULT_CONNECTION));
		System.out.println("\n [INFO] Setting endpoint to " + DEFAULT_CONNECTION);
		this.credentials = this.getCredentials(walletPass, walletSource);
		this.gasProvider = new StaticGasProvider(BigInteger.valueOf(20000000000L), BigInteger.valueOf(6721975L));
	}

	/**
	 * Constructor with default gas values.
	 * 
	 * @param walletPass        password to unlock wallet
	 * @param walletSource      path to wallet
	 * @param connectionAddress http-address to connect web3j to
	 * @throws CipherException 
	 * @throws IOException 
	 */
	public Web3jClient(String walletPass, String walletSource, String connectionAddress) throws IOException, CipherException {
		this.web3j = Web3j.build(new HttpService(connectionAddress));
		System.out.println("\n [INFO] Setting endpoint to " + connectionAddress);
		this.credentials = this.getCredentials(walletPass, walletSource);
		this.gasProvider = new StaticGasProvider(BigInteger.valueOf(20000000000L), BigInteger.valueOf(6721975L));

	}

	/**
	 * Load the credentials from the wallet file specified via <b>source</b>.
	 * 
	 * @param password to unlock wallet
	 * @param source   path to wallet
	 * @return Credentials
	 * @throws CipherException 
	 * @throws IOException 
	 */
	private Credentials getCredentials(String password, String source) throws IOException, CipherException {
		System.out.println(" [INFO] Loading credentials ...");
		return WalletUtils.loadCredentials(password, source);
	}

	/**
	 * Load a HashStore-Contract from the specified hex contract address. It also
	 * checks for validity of the contract. If loading was not possible, null is
	 * returned.
	 * 
	 * @param contractAddress as hex
	 * @return <b>HashStore</b> or <b>null</b>, if unsuccessful.
	 */
	public HashStore loadContract(String contractAddress) {
		HashStore contract = null;
		System.out.println("\nLoading contract ...");
		try {
			contract = HashStore.load(contractAddress, web3j, this.credentials, this.gasProvider);
			System.out.println("Contract Address: " + contract.getContractAddress() + " (valid : "
					+ this.validateContract(contract) + ")\n");
		} catch (EnsResolutionException e) {
			System.err.println("Contract not available at " + contractAddress + "!");
		}
		return contract;
	}

	/**
	 * Deploy a new contract instance of a HashStore.
	 * 
	 * @return <b>HashStore</b>
	 */
	public HashStore deployNewContract() {
		System.out.println("\nDeploying new contract ...");
		HashStore contract = null;
		try {
			contract = HashStore.deploy(this.web3j, this.credentials, this.gasProvider).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contract;
	}
	
	/**
	 * Get the wallet's address.
	 * 
	 * @return <b>String</b> wallet address
	 */
	public String getWalletAddress() {
		return this.credentials.getAddress();
	}

	/**
	 * Get the connection's client version.
	 * 
	 * @return <b>web3ClientVersion</b>
	 */
	public Web3ClientVersion getClientVersion() {
		Web3ClientVersion web3ClientVersion = null;
		try {
			web3ClientVersion = this.web3j.web3ClientVersion().send();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return web3ClientVersion;
	}

	/**
	 * Validity of the contract:<br>
	 * Check that the contract deployed at the address associated is in fact the
	 * contract you believe it is.
	 * 
	 * @param contract
	 * @return <b>boolean</b>
	 */
	private boolean validateContract(HashStore contract) {
		try {
			return contract.isValid();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}