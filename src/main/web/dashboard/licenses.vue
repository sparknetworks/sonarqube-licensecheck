<template>
  <div class="boxed-group boxed-group-inner">
    <h3>Licenses</h3>
    <p>This is a list of all licenses used in any dependencies listed above.</p>
    <table class="data zebra">
      <thead>
        <tr>
          <th>Identifier</th>
          <th>Name</th>
          <th>Allowed</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="license in licenses">
          <td>{{license.identifier}}</td>
          <td>{{license.name}}</td>
          <td>
            <span :class="{ 'icon-license-ok': license.status , 'icon-license-nok': !license.status }"></span>
            <span v-if='license.status'>
            Allowed
            </span>
            <span v-else>
            Forbidden
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
  export default {
    props: ['licenses']
  }
</script>

<style>
  @import "icons.css";
</style>
