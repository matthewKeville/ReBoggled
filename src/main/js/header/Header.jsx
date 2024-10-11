import React, { useState } from 'react';

import AccountControls from './AccountControls.jsx'

import styles from './Header.module.css'
import { Container, Group } from '@mantine/core';
import NavBar from './NavBar.jsx';
import PlayNow from '/src/main/js/header/PlayNow.jsx'

export default function Header() {

  return (
    <header className={styles.header}>
      <Container className={styles.inner}>

        <Group>
          <h1>ReBoggled</h1>
          <NavBar />
        </Group>
        <Group>
          <PlayNow />
        </Group>

        <Group>
          <AccountControls />
        </Group>


      </Container>
    </header>



  )
}
