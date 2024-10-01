import {state} from "./state.js";

document.getElementById('postCreateBtn').addEventListener('click',() => {

    const currentScrollPosition = window.scrollY;
    const currentPage = state.currentPage;
    console.log('Saving scroll position:', currentScrollPosition);
    console.log('Saving current page:', currentPage);

    localStorage.setItem('scrollPosition', currentScrollPosition);
    localStorage.setItem('currentPage', currentPage);
    window.location.href = '/postCreate';
})