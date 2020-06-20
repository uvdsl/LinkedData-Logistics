<template>
  <div>
    <div class="fix">
      <md-toolbar class=" md-primary" md-elevation="0">
        <h3 class="md-title" style="flex: 1">
          <md-icon>account_circle</md-icon>
          {{ profile.name }}
        </h3>
      </md-toolbar>

      <md-toolbar class="fix background-inherit md-dense" md-elevation="0">
        <md-button class="md-icon-button md-primary" @click="update">
          <md-icon> refresh </md-icon>
        </md-button>
        <span class="spacer" />
        <md-menu md-direction="bottom-start">
          <md-button md-menu-trigger class="md-icon-button md-primary">
            <md-icon>filter_list</md-icon>
          </md-button>

          <md-menu-content>
            <md-menu-item>
              <md-checkbox v-model="filter.offers" class="md-primary">
                Offers (to accept)
              </md-checkbox>
            </md-menu-item>
            <md-menu-item>
              <md-checkbox v-model="filter.accepts" class="md-primary">
                Accepts (to extend)
              </md-checkbox>
            </md-menu-item>
          </md-menu-content>
        </md-menu>
      </md-toolbar>
      <md-divider></md-divider>
    </div>
    <transition-group name="list" tag="md-list">
      <InboxItem
        v-for="item of filteredItems"
        :key="item.uri"
        :item="item"
        :hasInboxFocus="focusURI === item.uri"
        class="list-item"
        @selectedItem="selectInboxFocusItem"
      />
    </transition-group>
  </div>
</template>

<script>
// @ is an alias to /src
import InboxItem from "@/components/InboxItem.vue";
import { AS } from "@/lib/constants.js";
import { getN3 } from "@/lib/n3Requests.js";
import { getBoxContentFromN3, getItemContentFromN3 } from "@/lib/n3Query.js";
export default {
  name: "Inbox",
  components: {
    InboxItem
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
    },
    deFocus: Boolean
  },
  data() {
    return {
      items: [], //  [ { uri, type, actor, target, object, seeAlso } ]
      focusURI: null,
      filter: {
        offers: true,
        accepts: true
      }
    };
  },
  created() {
    this.update();
  },
  computed: {
    filteredItems: function() {
      return this.items.filter(item => {
        return (
          (item.type === AS("Offer") && this.filter.offers) ||
          (item.type === AS("Accept") && this.filter.accepts)
        );
      });
    }
  },
  watch: {
    deFocus: function() {
      if (this.deFocus) {
        console.log("### Inbox\t| DeFocus\n");
        this.focusURI = null;
        this.update();
      }
    }
  },
  methods: {
    async update() {
      // this.items = []; // better compare item URIs...
      var uriPromise = this.getItemURIs();
      if (this.items.length > 0) {
        await uriPromise
          .then(uris =>
            this.items
              .map(item => item.uri)
              .filter(itemURI => !uris.includes(itemURI))
          )
          .then(this.removeItems);
      }
      await uriPromise
        .then(uris =>
          uris.filter(
            itemURI => !this.items.map(item => item.uri).includes(itemURI)
          )
        )
        .then(this.addItems);
    },

    async getItemURIs() {
      console.log("### Inbox\t| Update LDP\n");
      var items = await getN3(this.profile.inbox).then(n3 =>
        getBoxContentFromN3(n3.store, n3.baseIRI)
      );
      if (this.profile.outbox !== this.profile.inbox) {
        items = items.concat(
          await getN3(this.profile.outbox).then(n3 =>
            getBoxContentFromN3(n3.store, n3.baseIRI)
          )
        );
      }
      return items;
    },

    async addItems(itemURIs) {
      console.log("### Inbox\t| Getting " + itemURIs.length + " Items\n");
      for (var uri of itemURIs) {
        getN3(uri)
          .then(n3 => getItemContentFromN3(n3.store, n3.baseIRI))
          .then(item => {
            if (item !== undefined) this.items.push(item);
          })
          .catch(err =>
            console.log("### Inbox\t| Fetching Error\n" + uri + "\n" + err)
          );
      }
    },

    async removeItems(itemURIs) {
      console.log("### Inbox\t| Removing " + itemURIs.length + " Items\n");
      for (var i = this.items.length - 1; i >= 0; --i) {
        if (itemURIs.indexOf(this.items[i].uri) !== -1) {
          this.items.splice(i, 1);
        }
      }
    },

    selectInboxFocusItem(item) {
      this.focusURI = item.uri;
      console.log("### Inbox\t| Selected\n" + this.focusURI);
      this.$emit("focusItem", item);
    }
  }
};
</script>

<style lang="scss" scoped>
.fix {
  z-index: 1000;
  position: sticky;
  top: 0px;
}
.spacer {
  flex: 1;
}
.background-inherit {
  background-color: white;
}

.list-item {
  transition: all 1s;
  // display: inline-block;
}
.list-enter {
  opacity: 0;
  transform: translateX(-30px);
}
.list-leave-to {
  opacity: 0;
  transform: translateY(80%);
}
// .list-leave-active {
//   position: absolute;
// }
.list-move {
  transition: all 1s;
}
</style>
