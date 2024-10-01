export function smoothScrollTo(element, duration) {
    const targetY = element.getBoundingClientRect().top + window.pageYOffset - 100; // Adjusting for fixed header if needed
    const startY = window.pageYOffset;
    const diffY = targetY - startY;
    let start;

    function step(timestamp) {
        if (!start) start = timestamp;
        const time = timestamp - start;
        const percent = Math.min(time / duration, 1);

        window.scrollTo(0, startY + diffY * easeInOutCubic(percent));

        if (time < duration) {
            window.requestAnimationFrame(step);
        }
    }

    function easeInOutCubic(t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    window.requestAnimationFrame(step);
}