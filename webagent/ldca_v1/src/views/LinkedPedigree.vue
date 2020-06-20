<template>
  <div>
    <transition-group name="list" tag="div">
      <LinkedPedigreePart
        v-for="part of parts"
        :key="part"
        class="list-item"
        :uri="part"
        :isLatest="part === item.seeAlso"
        :profile="profile"
        @prevPart="addPrevPart"
        @hasBeenApproved="approvedOnTrueRejectedOnFalse"
        @extend="extendGetTargetAndPass"
      />
    </transition-group>
    <Dialog
      :request="question"
      :showDialog="prompt"
      @returnValue="responseFunc"
    />
  </div>
</template>

<script>
import Dialog from "@/components/Dialog.vue";
import LinkedPedigreePart from "@/components/LinkedPedigreePart.vue";
import { approveHash, storeHash } from "@/lib/web3Requests.js";
import { getN3, postN3, hashN3, deleteN3 } from "@/lib/n3Requests.js";
import { getProfileDataFromN3, getContractAddress } from "@/lib/n3Query.js";
import {
  getTextTurtle,
  createActivityStreamItem,
  createLinkedPedigreePart
} from "@/lib/n3Creator.js";
export default {
  name: "LinkedPedigree",
  components: {
    LinkedPedigreePart,
    Dialog
  },
  props: {
    item: {
      // uri: String,
      // type: String,
      // actor: String,
      // target: String,
      // object: String,
      // seeAlso: String
    },
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
      parts: [],
      prompt: false,
      question: null,
      responseFunc: null,
      targetURI: null
    };
  },
  created() {
    this.parts.length = 0;
    this.parts.push(this.item.seeAlso);
  },
  computed: {},
  watch: {
    item: function() {
      this.parts.length = 0;
      this.parts.push(this.item.seeAlso);
    }
  },
  methods: {
    addPrevPart(prevPart) {
      this.parts.push(prevPart);
    },
    async approvedOnTrueRejectedOnFalse(approval_flag) {
      if (approval_flag) {
        this.ask("Wallet Password?", this.accept);
        // this.accept();
      } else {
        this.reject();
      }
    },
    async accept(pass) {
      this.prompt = false;
      console.log("### LP \t\t| Accepted\n" + this.item.uri);
      return getN3(this.item.seeAlso)
        .then(n3 => getContractAddress(n3.store, n3.baseIRI))
        .then(contractAddress =>
          approveHash(
            this.item.seeAlso, // pedigree uri
            contractAddress,
            this.profile.wallet,
            pass,
            this.profile.ledger
          )
        )
        .then(() =>
          createActivityStreamItem(
            "Accept",
            this.item.target,
            this.item.actor,
            this.item.object,
            this.item.seeAlso
          )
        )
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => postN3(this.profile.outbox, ttl))
        .then(loc =>
          console.log(
            "### LP \t\t| Accept of \n" +
              this.item.uri +
              "\n### LP \t\t| Available at\n" +
              loc
          )
        )
        .then(() => deleteN3(this.item.uri))
        .then(() => this.$emit("deletedURI", this.item.uri));
    },
    async reject() {
      console.log("### LP \t\t| Rejected\n" + this.item.uri);
      return createActivityStreamItem(
        "Reject",
        this.item.target,
        this.item.actor,
        this.item.object,
        this.item.seeAlso
      )
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => postN3(this.profile.docbox, ttl)) // or get the inbox of the sender
        .then(loc =>
          console.log(
            "### LP \t\t| Reject of \n" +
              this.item.uri +
              "\n### LP \t\t| Available at\n" +
              loc
          )
        )
        .then(() => deleteN3(this.item.uri))
        .then(() => this.$emit("deletedURI", this.item.uri));
    },
    extendGetTargetAndPass() {
      this.ask("Receiving Target URI?", this.extendGetPass);
    },
    extendGetPass(targetURI) {
      this.prompt = false;
      if (!targetURI.startsWith("http://")) {
        // default shortcut
        targetURI = "http://aifb-ls3-vm4.aifb.kit.edu:9999/rdf/profile-".concat(
          targetURI
        );
      }
      this.targetURI = targetURI; // hässlich wie die Nacht, fuck me right?
      this.ask("Wallet Password?", this.extend);
    },
    async extend(pass) {
      this.prompt = false;
      console.log("### LP \t\t| Extend\n" + this.item.uri);
      var target = await getN3(this.targetURI).then(n3 =>
        getProfileDataFromN3(n3.store, n3.baseIRI)
      );
      this.prompt = false;

      var n3PedigreePromise = createLinkedPedigreePart(
        this.profile.uri,
        this.item.object,
        this.item.seeAlso
      ); // new pedigree part

      var contractAddress = await n3PedigreePromise.then(n3 =>
        getContractAddress(n3.store, n3.baseIRI)
      );
      var newLPPLocation = await n3PedigreePromise
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => postN3(this.profile.docbox, ttl)); // new pedigree part

      var hash = await getN3(newLPPLocation)
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl =>
          hashN3(this.profile.hasher + "/hasher", ttl, newLPPLocation)
        )
        .then(n3 => n3.store.getQuads(null, null, null, null)[0].object.value);

      storeHash(
        newLPPLocation, // uri // new pedigree part
        this.item.seeAlso, // prevURI, null // not needed, is initial
        hash, // hash, // new pedigree part ttl
        target.wallet, // nextOwnerWallet, this.target.wallet
        contractAddress, // contractAddress, // new pedigree part n3 getContractAddress
        this.profile.wallet, // wallet, this.profile.wallet
        pass, // pass, Dialog
        this.profile.ledger // ledgerCon, this.profile.ledger
      )
        .then(() =>
          createActivityStreamItem(
            "Offer",
            this.profile.uri,
            target.uri,
            this.item.object,
            newLPPLocation
          )
        )
        .then(n3 => getTextTurtle(n3.store))
        .then(ttl => postN3(target.inbox, ttl))
        .then(loc =>
          console.log(
            "### LP \t\t| Offer of \n" +
              this.item.object +
              "\n### LP \t\t| Available at\n" +
              loc
          )
        )
        .then(() => deleteN3(this.item.uri))
        .then(() => this.$emit("deletedURI", this.item.uri));
    },
    ask(question, respFunc) {
      this.question = question;
      this.responseFunc = respFunc;
      this.prompt = true;
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
.list-item {
  transition: all 1s;
  // display: inline-block;
}
.list-enter {
  opacity: 0;
  transform: translateX(20%);
}
.list-leave-to {
  opacity: 0;
  transform: translateY(500%);
}
.list-leave-active {
  position: absolute;
}
.list-move {
  transition: all 1s;
}
</style>
