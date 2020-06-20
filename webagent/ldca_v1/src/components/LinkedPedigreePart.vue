<template>
  <div class="pad">
    <md-card>
      <div class="query-wrapper">
        <md-progress-bar v-if="isLoading" md-mode="query"></md-progress-bar>
      </div>
      <md-card-header>
        <md-avatar
          class="md-avatar-icon"
          v-bind:class="[
            !isValidated
              ? ''
              : hasValidatedHash && hasValidatedApproval && hasValidatedPrevPart
              ? 'md-primary'
              : 'md-accent'
          ]"
        >
          <md-icon>description</md-icon>
        </md-avatar>
        <div class="slim">
          <div class="spacer">
            <div class="md-title">Linked Pedigree Part</div>
            <div class="md-subhead">{{ uri }}</div>
          </div>
          <div>
            <md-button
              v-if="isLatest && isValidated && !hasValidatedApproval"
              class="md-accent md-dense md-raised"
              @click="reject()"
            >
              Reject
            </md-button>
            <md-button
              v-if="isLatest && isValidated && hasValidatedApproval"
              class="md-primary md-dense md-raised"
              @click="extend()"
            >
              Extend
            </md-button>
          </div>
        </div>
      </md-card-header>
      <md-card-content>
        <md-list>
          <md-list-item>
            <span>
              <md-icon
                v-bind:class="[
                  !isValidated
                    ? ''
                    : hasValidatedHash
                    ? 'md-primary'
                    : 'md-accent'
                ]"
              >
                {{
                  !isValidated
                    ? "help_outline"
                    : hasValidatedHash
                    ? "check_box"
                    : "report_problem"
                }}
              </md-icon>
            </span>
            <strong class="spacer mini-space">Hash</strong>
            <span>{{ hash }}</span>
          </md-list-item>
          <md-list-item>
            <span>
              <md-icon
                v-bind:class="[
                  !isValidated
                    ? ''
                    : hasValidatedApproval
                    ? 'md-primary'
                    : 'md-accent'
                ]"
              >
                {{
                  !isValidated
                    ? "help_outline"
                    : hasValidatedApproval
                    ? "check_box"
                    : "report_problem"
                }}
              </md-icon>
            </span>
            <strong class="spacer mini-space">Approval</strong>
            <span v-if="hasValidatedApproval">{{ hasValidatedApproval }}</span>
            <md-button
              v-if="isLatest && isValidated && !hasValidatedApproval"
              class="md-primary md-dense md-raised"
              @click="accept()"
            >
              Approve
            </md-button>
          </md-list-item>
          <md-list-item>
            <span>
              <md-icon
                v-bind:class="[
                  !isValidated
                    ? ''
                    : hasValidatedPrevPart
                    ? 'md-primary'
                    : 'md-accent'
                ]"
              >
                {{
                  !isValidated
                    ? "help_outline"
                    : hasValidatedPrevPart
                    ? "check_box"
                    : "report_problem"
                }}
              </md-icon>
            </span>
            <strong class="spacer mini-space">Previous</strong>
            <span>{{ prevPart ? prevPart : "none" }}</span>
          </md-list-item>
        </md-list>
      </md-card-content>
    </md-card>
  </div>
</template>

<script>
import { getValidationInfo } from "@/lib/web3Requests.js";
import { getN3, hashN3 } from "@/lib/n3Requests.js";
import { getPedigreeInfo, getContractAddress } from "@/lib/n3Query.js";
import { getTextTurtle } from "@/lib/n3Creator.js";
import { PDGR } from "@/lib/constants.js";
export default {
  name: "LinkedPedigreePart",
  props: {
    isLatest: Boolean,
    uri: String,
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
      hash: undefined,
      prevPart: undefined,
      contractAddress: null,
      isValidated: false,
      hasValidatedHash: false,
      hasValidatedApproval: false,
      hasValidatedPrevPart: false
    };
  },
  async created() {
    console.log("### LPP \t| Create\n" + this.uri);
    var n3Promise = getN3(this.uri);
    await Promise.all([
      this.setPreviousPart(n3Promise),
      this.setHash(n3Promise),
      this.setContractAddress(n3Promise)
    ]);
    this.validate();
  },
  computed: {},
  methods: {
    validate() {
      getValidationInfo(this.uri, this.contractAddress, this.profile.ledger)
        .then(this.checkOut)
        .then(() => (this.isValidated = true));
    },
    async setPreviousPart(n3Promise) {
      return n3Promise
        .then(n3 => getPedigreeInfo(n3.store, n3.baseIRI))
        .then(pdgrInfo => {
          if (
            pdgrInfo.prevPart === undefined &&
            pdgrInfo.status !== PDGR("Initial")
          ) {
            throw "### LPP \t| Linked Data incomplete at\n" + this.uri;
          }
          this.prevPart = pdgrInfo.prevPart;
          if (this.prevPart !== undefined) {
            this.$emit("prevPart", this.prevPart);
          }
        });
    },
    async setHash(n3Promise) {
      return n3Promise
        .then(n3 => getTextTurtle(n3.store, n3.baseIRI))
        .then(ttl => hashN3(this.profile.hasher + "/hasher", ttl, this.uri))
        .then(
          n3 =>
            (this.hash = n3.store.getQuads(
              null,
              null,
              null,
              null
            )[0].object.value)
        );
    },
    async setContractAddress(n3Promise) {
      return n3Promise
        .then(n3 => getContractAddress(n3.store, n3.baseIRI))
        .then(ctrctAddr => (this.contractAddress = ctrctAddr));
    },
    async checkOut(valInfo) {
      this.hasValidatedHash = this.hash == valInfo.hash;
      this.hasValidatedPrevPart = this.prevPart = valInfo.prevPart;
      this.hasValidatedApproval = valInfo.approval;
      // valInfo.owner
    },
    accept() {
      this.isLoading = true;
      console.log("### LPP \t| Approved -> Accepted\n" + this.uri);
      this.$emit("hasBeenApproved", true);
    },
    reject() {
      this.isLoading = true;
      console.log("### LPP \t| Not Approved -> Rejected\n" + this.uri);
      this.$emit("hasBeenApproved", false);
    },
    extend() {
      this.isLoading = true;
      console.log("### LPP \t| Extend\n" + this.uri);
      this.$emit("extend", true);
    }
  }
};
</script>

<style lang="scss" scoped>
.pad {
  padding: 5px;
}
.slim {
  display: flex;
  align-items: flex-start;
  vertical-align: middle;
  margin: 0px;
  padding: 5px 10px 5px 10px;
}
.vert-align {
  vertical-align: middle;
}
.intented {
  margin-left: 55px;
}
.mini-space {
  margin-left: 5px;
}
.float {
  float: left;
}
.query-wrapper {
  max-height: 0px;
}
</style>
