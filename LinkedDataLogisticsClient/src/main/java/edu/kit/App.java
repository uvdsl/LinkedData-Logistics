package edu.kit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.web3j.crypto.CipherException;

/*
 * 
 * 
 * Init Blockchain: geth --datadir ethereum init ethereum/genesis.json // already done
 * Copy keystore // already done
 * Smart Contract available at 0x0cc602df7db83fa106e2978a4e91dd0d097e48b8
 * 
 * Start node: 	geth --port 3000 --networkid 58343 --nodiscover --datadir=ethereum --maxpeers=0  --rpc --rpcport 8543 --rpcapi "eth,net,web3,personal,miner"
 * Access node: geth attach http://localhost:8543
 * 
 * personal.unlockAccount("0x401cbc58de13cdc43a3da1c557d0583f43deaa1d", "dld") // for deployment of smart contracts via https://remix.ethereum.org/#optimize=false&version=soljson-v0.5.3+commit.10d17f24.js
 * "0xe495c94b8776a1550e79923f1e7f4aa21013fd39", "ldl"
 * miner.start(1)
 * miner.stop(1)
 * 
 * 
 * Various:
 * miner.setEtherbase("0x401cbc58de13cdc43a3da1c557d0583f43deaa1d")
 * web3.fromWei(eth.getBalance("0x401cbc58de13cdc43a3da1c557d0583f43deaa1d"), "ether")
 * web3j solidity generate -a F:\Users\Chris\Programming\workspace\LinkedDataLogistics\LinkedDataLogisticsClient\src\main\resources\contract\LDHashNotary.abi -b F:\Users\Chris\Programming\workspace\LinkedDataLogistics\LinkedDataLogisticsClient\src\main\resources\contract\LDHashNotary.bin -p edu.kit.eth -o F:\Users\Chris\Programming\workspace\LinkedDataLogistics\LinkedDataLogisticsClient\src\main\java\
 */

/**
 *
 * @author Christoph
 *
 */
public class App {

	// BIRTH CERT : http://www.student.kit.edu/~uvdsl/data/birthCert.ttl
	// BIRTH HASH : 0x4892db195c21090e39e04d2c8ea6f59e
	// TRANSFER 1 : http://www.student.kit.edu/~uvdsl/data/linkedTransfer1.ttl
	// WALLET HOME: ethereum/keystore/ldlWallet.json

	private static int counter = 0;
	private static BufferedReader cmd = new BufferedReader(new InputStreamReader(System.in));
	private static AppService service = new AppService();

	public static void main(String[] args) {
		System.out.println(" [INFO] Starting LinkedData-Client ...");
		String command = null;
		while (true) {
			try {
				Thread.sleep(10);
				command = request("\nTell me what you want!");
				switch (command) {
				case ("switchWallets"):
					switchWallets();
					break;
				case ("newProduct"):
					System.out.println("Gas used: " + newProduct());
					break;
				case ("linkedTransfer"):
					linkedTransfer();
					break;
				case ("linkedPedigree"):
					linkedPedigree().forEach(
							mapping -> mapping.forEach((key, value) -> System.out.println("\n" + key + "\n" + value)));
					break;
				case ("checkPedigree"):
					checkPedigree().forEach(
							mapping -> mapping.forEach((key, value) -> System.out.println("\n" + key + "\n" + value)));
					break;
				case "hashDL":
					hashDL();
					break;
				case ("exit"):
					exit();
					break;
				case ("cry louder"):
					System.out.println("\n [LINK] https://http.cat/420");
				case ("cry"):
				case ("cry for help"):
				case ("help me"):
					System.out.println("\n [ECHO] You are not alone! :)");
				case ("help"):
					System.out.println("\n [COMMAND LIST] \n\t"
							+ "switchWallets\tswitch to a different ethereum wallet\n\t"
							+ "newProduct\tfetch adds new products to the smart contract\n\t"
							+ "linkedTransfer\tconduct a full linked transfer on the ethereum blockchain\n\t"
							+ "linkedPedigree\tfetch a linked pedigree from the LOC\n\t"
							+ "checkPedigree\tcheck a linked pedigree from the LOC on the ethereum blockchain\n\t"
							+ "hashDL\t\thash linked data identified by an uri\n\t" + "help\t\tdisplay this message\n\t"
							+ "exit\t\thave a nice day!");
					break;
				default:
					System.out.println("\nTry again or cry for help ...");
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.err
						.println("\n [ERROR] You broke it." + ((++counter > 1) ? " Again ... [x" + counter + "]" : ""));
				System.err.println("\t (" + e.getMessage() + " !)");
			}
		}
	}

	static void switchWallets() {
		String wallet_home = "";
		String wallet_pass = "";
		try {
			Thread.sleep(10);
			wallet_home = request("\nPlease enter your WALLET_HOME:");
			wallet_pass = request("\nPlease enter your WALLET_PASS:");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		try {
			service.switchWallets(wallet_pass, wallet_home);
		} catch (CipherException e) {
			System.err.println("\n [ERROR] You better remember your password ...");
			switchWallets();
		} catch (IOException e) {
			System.err.println("\n [ERROR] Looks like your wallet is gone ...");
			switchWallets();
		}
	}

	static String newProduct() throws IOException {
		String birthCertURI = request("\nPlease enter the product's birth certificate uri:");
		String address = request("\nPlease enter the product's new owner address:");
		return service.registerNewProduct(birthCertURI, address);
	}

	static void linkedTransfer() throws IOException, InterruptedException {
		List<Map<String, Boolean>> checkedPedigree = service.checkLinkedPedigree(linkedPedigree());
		checkedPedigree
				.forEach(mapping -> mapping.forEach((key, value) -> System.out.println("\n" + key + "\n" + value)));
		if (requestConfirmation("\nContinue?")) {
			String newTransferDocURI = request("\nPlease enter the URI of the product's new transfer document:");
			String address = request("\nPlease enter the product's new owner address:");
			String birthCertURI = (String) checkedPedigree.get(0).keySet().toArray()[0];
			System.out.println(service.linkedTransfer(birthCertURI, newTransferDocURI, address));
		}
	}

	static List<Map<String, String>> linkedPedigree() throws IOException {
		String lastTransferDocURI = request("\nPlease enter the product's last transfer document uri:");
		return service.fetchFullLinkedPedigree(lastTransferDocURI);
	}

	static List<Map<String, Boolean>> checkPedigree() throws IOException {
		return service.checkLinkedPedigree(linkedPedigree());
	}

	static void hashDL() throws IOException {
		service.hashURI(request("\nPlease enter the URI to hash:"));
	}

	static String request(String request) throws IOException {
		System.out.println(request);
		return strip(cmd.readLine());
	}

	static boolean requestConfirmation(String message) throws IOException, InterruptedException {
		while (true) {
			Thread.sleep(10);
			String input = request("\n" + message + "\nConfirm (y/n):");
			switch (input) {
			case ("y"):
			case ("yes"):
				return true;
			case ("n"):
			case ("no"):
				return false;
			default:
				System.err.println("\n [ERROR] Invalid input!");
			}
		}
	}

	static void exit() {
		System.out.println("\nBye! <3");
		System.exit(0);
	}
	
	private static String strip(String input) {
		if (input.startsWith(" ")) {
			input = input.substring(1);
			return strip(input);
		} 
		if (input.endsWith(" ")) {
			input = input.substring(0, input.length() - 1);
			return strip(input);
		} 
		return input;
	}

}
