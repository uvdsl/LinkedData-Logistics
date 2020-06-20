<template>
  <div @click="select">
    <md-card md-with-hover>
      <md-card-header class="slim">
        <md-icon
          class="md-size-2x"
          v-bind:class="[hasInboxFocus ? 'md-accent' : 'md-primary']"
        >
          {{ isAccepted ? "assignment_turned_in" : "assignment_late" }}
        </md-icon>
        <div class="intented flex">
          <div class="md-title">Linked Offer</div>
          <div class="md-subhead">
            <div>
              From : {{ isAccepted ? targetData.name : actorData.name }}
            </div>
            <div>Object : {{ objectData.name }}</div>
          </div>
        </div>
      </md-card-header>
    </md-card>
  </div>
</template>

<script>
import { AS } from "@/lib/constants.js";
import { getN3 } from "@/lib/n3Requests.js";
import { getLabelFromN3 } from "@/lib/n3Query.js";
export default {
  name: "InboxItem",
  props: {
    item: {
      // uri: String,
      // type: String,
      // actor: String,
      // target: String,
      // object: String,
      // seeAlso: String
    },
    hasInboxFocus: Boolean
  },
  data() {
    return {
      isAccepted: false,
      actorData: { name: null },
      targetData: { name: null },
      objectData: { name: null }
    };
  },
  created() {
    console.log("### BoxItem\t| I Am Legend\n" + this.item.uri);
    getN3(this.item.actor)
      .then(n3 => getLabelFromN3(n3.store, n3.baseIRI))
      .then(name => (this.actorData.name = name));
    getN3(this.item.target)
      .then(n3 => getLabelFromN3(n3.store, n3.baseIRI))
      .then(name => (this.targetData.name = name));
    getN3(this.item.object)
      .then(n3 => getLabelFromN3(n3.store, n3.baseIRI))
      .then(name => (this.objectData.name = name));
    this.isAccepted = this.item.type === AS("Accept");
  },
  computed: {},
  methods: {
    printURI() {
      console.log(this.item.uri);
    },

    select() {
      // console.log("### BoxItem\t| Selected!\n" + this.item.uri);
      if (!this.hasInboxFocus) this.$emit("selectedItem", this.item);
    }
  }
};
</script>

<style lang="scss" scoped>
.slim {
  display: flex;
  align-items: flex-start;
  vertical-align: middle;
  margin: 0px;
  padding: 5px 10px 5px 10px;
}
.align {
  vertical-align: middle;
}
.intented {
  margin-left: 20px;
}
.flex {
  flex: 1;
}
</style>
