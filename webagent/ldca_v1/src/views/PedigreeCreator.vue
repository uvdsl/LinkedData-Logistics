<template>
  <div>
    <md-card>
      <div class="query-wrapper">
        <md-progress-bar v-if="isLoading" md-mode="query"></md-progress-bar>
      </div>
      <md-card-header>
        <md-avatar class="md-avatar-icon">
          <md-icon>description</md-icon>
        </md-avatar>
        <div class="slim">
          <div class="spacer">
            <div class="md-title">New Linked Pedigree</div>
            <div class="md-subhead">Create the Initial Part!</div>
          </div>
          <div>
            <md-button
              class="md-primary md-dense md-raised"
              @click="askForWalletPass"
            >
              Create
            </md-button>
          </div>
        </div>
      </md-card-header>
      <md-card-content>
        <md-field>
          <label>Sent Object</label>
          <md-input v-model="object"></md-input>
        </md-field>
        <md-field>
          <label>Receiving Target</label>
          <md-input v-model="target.uri"></md-input>
        </md-field>
      </md-card-content>
    </md-card>
    <Dialog
      request="Wallet Password:"
      :showDialog="prompt"
      @returnValue="create"
    />
  </div>
</template>

<script>
import Dialog from "@/components/Dialog.vue";
import { storeHash } from "@/lib/web3Requests.js";
import { getN3, postN3, hashN3 } from "@/lib/n3Requests.js";
import { getProfileDataFromN3, getContractAddress } from "@/lib/n3Query.js";
import {
  getTextTurtle,
  createLinkedPedigreePart,
  createActivityStreamItem
} from "@/lib/n3Creator.js";
export default {
  name: "PedigreeCreator",
  components: {
    Dialog
  },
  props: {
    profile: {
      uri: String,
      name: String,
      depiction: String,
      inbox: String,
      outbox: String,
      docbox: String,
      wallet: String,
      ledger: String,
      hasher: String
    }
  },
  data() {
    return {
      isLoading: false,
      prompt: false,
      object: "http://aifb-ls3-vm4.aifb.kit.edu:9999/rdf/cat",
      target: {
        uri: "http://aifb-ls3-vm4.aifb.kit.edu:9999/rdf/profile-trucker"
      },
      seeAlso: null
    };
  },
  methods: {
    askForWalletPass() {
      this.prompt = true;
    },
    async create(pass) {
      this.prompt = false;
      this.isLoading = true;
      console.log(
        "### Creator \t| Create initial LPP\n" +
          "\nObject:\n" +
          this.object +
          "\nTarget:\n" +
          this.target.uri
      );
      this.target = await getN3(this.target.uri).then(n3 =>
        getProfileDataFromN3(n3.store, n3.baseIRI)
      ); // get Target Profile

      var n3PedigreePromise = createLinkedPedigreePart(
        this.profile.uri,
        this.object,
        null
      ); // new pedigree part

      var contractAddress = await n3PedigreePromise.then(n3 =>
        getContractAddress(n3.store, n3.baseIRI)
      );
      this.seeAlso = await n3PedigreePromise
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => postN3(this.profile.docbox, ttl)); // new pedigree part

      var hash = await getN3(this.seeAlso)
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => hashN3(this.profile.hasher + "/hasher", ttl, this.seeAlso))
        .then(n3 => n3.store.getQuads(null, null, null, null)[0].object.value);

      // wait for everything

      storeHash(
        this.seeAlso, // uri, this.seeAlso // new pedigree part
        null, // prevURI, null // not needed, is initial
        hash, // hash, // new pedigree part ttl
        this.target.wallet, // nextOwnerWallet, this.target.wallet
        contractAddress, // contractAddress, // new pedigree part n3 getContractAddress
        this.profile.wallet, // wallet, this.profile.wallet
        pass, // pass, Dialog
        this.profile.ledger // ledgerCon, this.profile.ledger
      )
        .then(() =>
          createActivityStreamItem(
            "Offer",
            this.profile.uri,
            this.target.uri,
            this.object,
            this.seeAlso
          )
        )
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => postN3(this.target.inbox, ttl))
        .then(loc =>
          console.log(
            "### LP \t\t| Offer of \n" +
              this.seeAlso +
              "\n### LP \t\t| Available at\n" +
              loc
          )
        )
        .finally(() => (this.isLoading = false));
    }
  }
};
</script>

<style scoped>
.slim {
  display: flex;
  align-items: flex-start;
  vertical-align: middle;
  margin: 0px;
  padding: 5px 10px 5px 10px;
}
.query-wrapper {
  height: 0px;
}
</style>
