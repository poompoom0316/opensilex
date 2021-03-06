<template>
  <div>
    <opensilex-StringFilter
      :filter.sync="filter"
      @update="updateFilter()"
      placeholder="component.group.filter-placeholder"
    ></opensilex-StringFilter>

    <opensilex-TableAsyncView
      ref="tableRef"
      :searchMethod="searchGroups"
      :fields="fields"
    >
      <template v-slot:cell(uri)="{data}">
        <opensilex-UriLink :uri="data.item.uri" @click="data.toggleDetails()"></opensilex-UriLink>
      </template>

      <template v-slot:cell(userProfiles)="{data}">
        <div>{{$tc("component.user.label", data.item.userProfiles.length, {count: data.item.userProfiles.length})}}</div>
      </template>

      <template v-slot:row-details="{data}">
        <strong class="capitalize-first-letter">{{$t("component.user.users")}}:</strong>
        <ul>
          <li
            v-for="userProfile in data.item.userProfiles"
            v-bind:key="userProfile.uri"
          >{{userProfile.userName}} ({{userProfile.profileName}})</li>
        </ul>
      </template>

      <template v-slot:cell(actions)="{data}">
        <b-button-group size="sm">
          <opensilex-DetailButton
            @click="data.toggleDetails()"
            label="component.group.details"
            :detailVisible="data.detailsShowing"
            :small="true"
          ></opensilex-DetailButton>
          <opensilex-EditButton
            v-if="user.hasCredential(credentials.CREDENTIAL_GROUP_MODIFICATION_ID)"
            @click="$emit('onEdit', data.item)"
            label="component.group.update"
            :small="true"
          ></opensilex-EditButton>
          <opensilex-DeleteButton
            v-if="user.hasCredential(credentials.CREDENTIAL_GROUP_DELETE_ID)"
            @click="deleteGroup(data.item.uri)"
            label="component.group.delete"
            :small="true"
          ></opensilex-DeleteButton>
        </b-button-group>
      </template>
    </opensilex-TableAsyncView>
  </div>
</template>

<script lang="ts">
import { Component, Ref } from "vue-property-decorator";
import Vue from "vue";
import HttpResponse, {
  OpenSilexResponse
} from "opensilex-security/HttpResponse";
import { SecurityService, GroupDTO } from "opensilex-security/index";

@Component
export default class GroupList extends Vue {
  $opensilex: any;
  $store: any;

  get user() {
    return this.$store.state.user;
  }

  get credentials() {
    return this.$store.state.credentials;
  }

  private filter: any = "";

  created() {
    let query: any = this.$route.query;
    if (query.filter) {
      this.filter = decodeURI(query.filter);
    }
  }

  updateFilter() {
    this.$opensilex.updateURLParameter("filter", this.filter, "");
    this.refresh();
  }

  fields = [
    {
      key: "uri",
      label: "component.common.uri",
      sortable: true
    },
    {
      key: "name",
      label: "component.common.name",
      sortable: true
    },
    {
      label: "component.common.description",
      key: "description",
      sortable: true
    },
    {
      label: "component.user.users",
      key: "userProfiles"
    },
    {
      label: "component.common.actions",
      key: "actions",
      class: "table-actions"
    }
  ];

  @Ref("tableRef") readonly tableRef!: any;

  refresh() {
    this.tableRef.refresh();
  }

  searchGroups(options) {
    return this.$opensilex
      .getService("opensilex.SecurityService")
      .searchGroups(
        this.filter,
        options.orderBy,
        options.currentPage,
        options.pageSize
      );
  }

  deleteGroup(uri: string) {
    this.$opensilex
      .getService("opensilex.SecurityService")
      .deleteGroup(uri)
      .then(() => {
        this.refresh();
        this.$emit("onDelete", uri);
      })
      .catch(this.$opensilex.errorHandler);
  }
}
</script>

<style scoped lang="scss">
</style>
